#!/usr/bin/env python3
"""Check SageTV's OpenDCT scan contract and an optional commissioned tuner."""

import os
import pathlib
import socket


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def request(stream, value):
    stream.write((value + "\r\n").encode("ascii"))
    reply = stream.readline()
    if not reply:
        raise RuntimeError("OpenDCT closed the connection without a response")
    return reply.decode("utf-8", errors="replace").strip()


def live_scan(host, port, encoder):
    found = []
    with socket.create_connection((host, port), timeout=10) as connection:
        connection.settimeout(120)
        stream = connection.makefile("rwb", buffering=0)
        require(request(stream, "VERSION") == "3.0",
                "commissioned OpenDCT endpoint is not protocol version 3.0")
        for index in range(4):
            reply = request(stream, "AUTOINFOSCAN %s|%d" % (encoder, index))
            if reply and reply != "ERROR":
                found.append(reply)
    require(found, "OpenDCT returned no channel data")


def main():
    source = pathlib.Path("java/sage/NetworkCaptureDevice.java").read_text(
        encoding="utf-8"
    )
    require('"AUTOINFOSCAN " + getLocalName() + "|" + tuneString' in source,
            "SageTV no longer sends the local encoder name for AUTOINFOSCAN")

    values = [
        os.environ.get("OPENDCT_TEST_HOST"),
        os.environ.get("OPENDCT_TEST_PORT"),
        os.environ.get("OPENDCT_TEST_ENCODER"),
    ]
    if any(values):
        require(all(values), "OPENDCT_TEST_HOST, OPENDCT_TEST_PORT, and "
                "OPENDCT_TEST_ENCODER must be set together")
        live_scan(values[0], int(values[1]), values[2])
        status = "PASS - commissioned endpoint returned channel data"
    else:
        status = "SKIPPED - no commissioned endpoint configured"

    pathlib.Path("output/test-results/opendct-live.status").write_text(
        status + "\n", encoding="utf-8"
    )
    print("[PASS] SageTV/OpenDCT AUTOINFOSCAN source contract")
    print("[%s] Live OpenDCT channel scan" % status)


if __name__ == "__main__":
    main()
