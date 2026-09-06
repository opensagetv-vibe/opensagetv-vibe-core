#!/bin/bash
git rev-list HEAD | sort > config.git-hash
LOCALVER=`wc -l config.git-hash | awk '{print $1}'`
if [ $LOCALVER \> 1 ] ; then
    # Forks use origin for the writable fork and upstream for the parent.
    # Derive the version without assuming origin/master exists.
    BASE_REF=""
    for CANDIDATE in upstream/master origin/master master ; do
        if git rev-parse --verify --quiet "$CANDIDATE^{commit}" >/dev/null ; then
            BASE_REF="$CANDIDATE"
            break
        fi
    done
    if [ -n "$BASE_REF" ] ; then
        VER=`git rev-list "$BASE_REF" | sort | join config.git-hash - | wc -l | awk '{print $1}'`
    else
        VER=$LOCALVER
    fi
    if [ $VER != $LOCALVER ] ; then
        VER="$VER+$(($LOCALVER-$VER))"
    fi
    if git status | grep -q "modified:" ; then
        VER="${VER}M"
    fi
    VER="$VER $(git rev-list HEAD -n 1 | cut -c 1-7)"
    echo "#define X264_VERSION \" r$VER\""
else
    echo "#define X264_VERSION \"\""
    VER="x"
fi
rm -f config.git-hash
API=`grep '#define X264_BUILD' < x264.h | sed -e 's/.* \([1-9][0-9]*\).*/\1/'`
echo "#define X264_POINTVER \"0.$API.$VER\""
