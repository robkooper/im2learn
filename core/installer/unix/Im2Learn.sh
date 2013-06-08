#!/bin/sh
IM2LEARN_DIR=`dirname $0`
CLASSPATH="$IM2LEARN_DIR/Im2Learn.jar"
for f in $IM2LEARN_DIR/ext/*.jar; do
  CLASSPATH="${CLASSPATH}:$f"
done
CLASSPATH="${CLASSPATH}:${IM2LEARN_DIR}"

java -Xms256M -Xmx512M -Djava.library.path=${IM2LEARN_DIR}/lib/`uname -s` -cp "${CLASSPATH}" @MAIN@ $*
