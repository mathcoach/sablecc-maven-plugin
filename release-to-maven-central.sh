#! /bin/bash
# Doku + Anleitung: https://central.sonatype.org/pages/support/

mvn -P'-nexus,central.sonatype.com' clean deploy

