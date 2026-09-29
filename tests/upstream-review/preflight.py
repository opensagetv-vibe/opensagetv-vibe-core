#!/usr/bin/env python3
"""Fail-closed preflight for canonical SageTV Core pull requests."""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[2]
DEFAULT_MANIFEST = ROOT / "docs" / "UPSTREAM_PR_TOPICS.json"


class PreflightError(RuntimeError):
    pass


def command(*args: str) -> str:
    result = subprocess.run(
        args,
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    if result.returncode:
        detail = result.stderr.strip() or result.stdout.strip()
        raise PreflightError(f"command failed ({' '.join(args)}): {detail}")
    return result.stdout.strip()


def git(*args: str) -> str:
    return command("git", *args)


def gh_json(*args: str) -> Any:
    text = command("gh", *args)
    return json.loads(text) if text else None


def split_lines(value: str) -> list[str]:
    return value.splitlines() if value else []


def require(condition: bool, message: str) -> None:
    if not condition:
        raise PreflightError(message)


def audit_topic(manifest: dict[str, Any], topic: dict[str, Any]) -> dict[str, Any]:
    target = manifest["target_repository"]
    base = manifest["base_branch"]
    remote = manifest["upstream_remote"]
    owner = manifest["head_owner"]
    branch = topic["branch"]
    reference = topic["reference_branch"]
    upstream_ref = f"{remote}/{base}"

    local_sha = git("rev-parse", branch)
    remote_line = git("ls-remote", "origin", f"refs/heads/{branch}")
    require(remote_line, f"{branch}: remote branch is missing")
    require(remote_line.split()[0] == local_sha, f"{branch}: local and origin heads differ")

    behind, ahead = (int(value) for value in git(
        "rev-list", "--left-right", "--count", f"{upstream_ref}...{branch}"
    ).split())
    require(behind == 0, f"{branch}: branch is {behind} commit(s) behind {upstream_ref}")
    require(ahead > 0, f"{branch}: branch has no proposed commits")
    require(not git("diff", "--check", f"{upstream_ref}..{branch}"), f"{branch}: git diff --check failed")

    expected = manifest["expected_identity"]
    identity = f"{expected['name']}|{expected['email']}|{expected['name']}|{expected['email']}"
    identities = split_lines(git(
        "log", "--format=%an|%ae|%cn|%ce", f"{upstream_ref}..{branch}"
    ))
    wrong = [value for value in identities if value != identity]
    require(not wrong, f"{branch}: unexpected author/committer identity: {wrong}")

    reference_base = git("merge-base", upstream_ref, reference)
    old_names = git("diff", "--name-status", f"{reference_base}..{reference}")
    new_names = git("diff", "--name-status", f"{upstream_ref}..{branch}")
    old_numstat = git("diff", "--numstat", f"{reference_base}..{reference}")
    new_numstat = git("diff", "--numstat", f"{upstream_ref}..{branch}")
    require(old_names == new_names, f"{branch}: changed-file set differs from {reference}")
    require(old_numstat == new_numstat, f"{branch}: per-file change counts differ from {reference}")

    comparison = gh_json(
        "api", f"repos/{target}/compare/{base}...{owner}:{branch}"
    )
    require(comparison.get("status") == "ahead", f"{branch}: GitHub compare status is {comparison.get('status')}")
    require(comparison.get("behind_by") == 0, f"{branch}: GitHub reports branch behind canonical base")
    commits = comparison.get("commits") or []
    require(commits, f"{branch}: GitHub compare returned no proposed commits")
    require(commits[-1].get("sha") == local_sha, f"{branch}: GitHub compare head differs")
    require(
        comparison["merge_base_commit"]["sha"] == git("rev-parse", upstream_ref),
        f"{branch}: GitHub merge base differs from current canonical base",
    )

    for forbidden in manifest.get("forbidden_targets", []):
        pulls = gh_json(
            "pr", "list", "--repo", forbidden, "--state", "open",
            "--head", f"{owner}:{branch}", "--json", "number",
        )
        require(not pulls, f"{branch}: open pull request exists against forbidden target {forbidden}")

    pr_number = topic.get("pr")
    if pr_number:
        pull = gh_json(
            "pr", "view", str(pr_number), "--repo", target,
            "--json", "state,isDraft,baseRefName,headRefName,headRefOid,url",
        )
        require(pull["state"] == "OPEN", f"PR #{pr_number}: not open")
        require(pull["baseRefName"] == base, f"PR #{pr_number}: wrong base")
        require(pull["headRefName"] == branch, f"PR #{pr_number}: wrong head branch")
        require(pull["headRefOid"] == local_sha, f"PR #{pr_number}: wrong head commit")
        require(pull["isDraft"] == topic["draft"], f"PR #{pr_number}: wrong draft state")

    return {
        "branch": branch,
        "head": local_sha,
        "ahead": ahead,
        "pr": pr_number,
        "status": "PASS",
    }


def require_pilot_green(manifest: dict[str, Any]) -> dict[str, Any]:
    target = manifest["target_repository"]
    pilots = [topic for topic in manifest["topics"] if topic.get("pilot")]
    require(len(pilots) == 1, "manifest must define exactly one pilot topic")
    pilot = pilots[0]
    require(pilot.get("pr"), "pilot PR has not been created")
    pull = gh_json(
        "pr", "view", str(pilot["pr"]), "--repo", target,
        "--json", "statusCheckRollup,url",
    )
    checks = {item.get("name"): item for item in pull["statusCheckRollup"]}
    results: dict[str, str] = {}
    for name in manifest["required_pilot_checks"]:
        require(name in checks, f"pilot PR is missing required check {name}")
        item = checks[name]
        state = f"{item.get('status')}/{item.get('conclusion')}"
        results[name] = state
        require(
            item.get("status") == "COMPLETED" and item.get("conclusion") == "SUCCESS",
            f"pilot PR check {name} is {state}; do not submit the remaining batch",
        )
    return {"pr": pilot["pr"], "url": pull["url"], "checks": results, "status": "PASS"}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument(
        "--require-pilot-green",
        action="store_true",
        help="also block unless every required pilot PR check has passed",
    )
    parser.add_argument("--skip-fetch", action="store_true")
    args = parser.parse_args()

    manifest_path = args.manifest.resolve()
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if not args.skip_fetch:
        command(
            "git", "fetch", manifest["upstream_remote"], manifest["base_branch"], "--quiet"
        )

    report = {"manifest": str(manifest_path), "topics": []}
    try:
        for topic in manifest["topics"]:
            report["topics"].append(audit_topic(manifest, topic))
        if args.require_pilot_green:
            report["pilot"] = require_pilot_green(manifest)
        report["status"] = "PASS"
        print(json.dumps(report, indent=2, sort_keys=True))
        return 0
    except (PreflightError, KeyError, TypeError, ValueError) as error:
        report["status"] = "FAIL"
        report["error"] = str(error)
        print(json.dumps(report, indent=2, sort_keys=True))
        return 1


if __name__ == "__main__":
    sys.exit(main())
