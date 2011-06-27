#!/bin/bash

java -Dfile.encoding=UTF-8 -Dinput.encoding=UTF-8 -classpath src:lib/\*:lib/dev/\* jline.ConsoleRunner clojure.main
