#!/bin/sh
# thank @xuwei-k
curl -O https://raw.github.com/paulp/sbt-extras/1dbab99024e2bec49b9171b61eac4d48654eaf26/sbt &&
chmod u+x ./sbt &&
./sbt -mem 512 +test