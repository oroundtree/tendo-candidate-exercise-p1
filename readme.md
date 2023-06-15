This repo contains a spark flow for transforming data as per the Tendo candidate exercise.

Can be ran directly from IntelliJ using the provided run configuration or follow these steps to build and submit to spark cluster:

First build the uber jar

`sbt assembly`

This can be found in `target/scala-2.12/tendo_flow.jar` and can be submitted to a cluster using spark-submit as follows

`spark-submit --class ExerciseFlow --master local[8] [PATH TO UBER JAR]`

Be sure to set `INPUT_PATH` and `OUTPUT_PATH` system variables to point to desired input and output files if you are running this outside the project directory