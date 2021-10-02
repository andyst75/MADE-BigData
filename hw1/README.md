# HW01

## Блок 1. Развертывание локального кластера Hadoop

![](image/hadoop1.png)
![](image/hadoop2.png)
![](image/hadoop3.png)

## Блок 2. Работа с HDFS

#### См. флаги “-mkdir” и “-touchz“

1. Создайте папку в корневой HDFS-папке

> hdfs dfs -mkdir /data


2. Создайте в созданной папке новую вложенную папку.

> hdfs dfs -mkdir /data/temp


3. Что такое Trash в распределенной FS? Как сделать так, чтобы файлы удалялись сразу, минуя “Trash”?

        При исполнении команды hdfs dfs -rm файлы не удаляются, а перемещаются в директорию Trash.
        Для немедленного удаления используется ключ -skipTrash.


4. Создайте пустой файл в подпапке из пункта 2.

> hdfs dfs -touchz /data/temp/text


5. Удалите созданный файл.

> hdfs dfs -rm -skipTrash /data/temp/text


6. Удалите созданные папки.

> hdfs dfs -rm -r -skipTrash /data


#### См. флаги “-put”, “-cat”, “-tail”, “-cp”

1. Скопируйте любой файл в новую папку на HDFS

> docker cp ./text docker-hadoop:/text
>
> hdfs dfs -put /text /data/ 


2. Выведите содержимое HDFS-файла на экран.

> hdfs dfs -cat /data/text



3. Выведите содержимое нескольких последних строчек HDFS-файла на экран.

> hdfs dfs -tail /data/text
>
> hdfs dfs -cat /data/text | tail -n 3


4. Выведите содержимое нескольких первых строчек HDFS-файла на экран.

> hdfs dfs -head /data/text
>
> hdfs dfs -cat /data/text | head -n 3


5. Переместите копию файла в HDFS на новую локацию.

> hdfs dfs -cp /data/text /text_copy


#### См. флаги “-setrep -w”, “-files - blocks -locations”

1. Изменить replication factor для файла. Как долго занимает время на увеличение / уменьшение числа реплик для файла?

    Создадим 3 реплики.
        
> hdfs dfs -setrep -w 3 /data/text

        Replication 3 set: /data/text
        Waiting for /data/text .... done
        Процесс занял порядка 10 секунд.

    Уменьшим количество реплик до 2х.
> hdfs dfs -setrep -w 2 /data/text

        Replication 2 set: /data/text
        Waiting for /data/text ...
        WARNING: the waiting time may be long for DECREASING the number of replications.
        . done
        
    Процесс уменьшения реплик занял порядка 15 секунд.


2. Найдите информацию по файлу, блокам и их расположениям.
> hdfs fsck /data/text -files -blocks -locations

        Connecting to namenode via http://namenode:9870/fsck?ugi=root&files=1&blocks=1&locations=1&path=%2Fdata%2Ftext

        FSCK started by root (auth:SIMPLE) from /172.19.0.5 for path /data/text at Sat Oct 02 06:26:19 UTC 2021
        ?/data/text 56 bytes, replicated: replication=3, 1 block(s):  OK

        0. BP-37593933-172.19.0.5-1633147495095:blk_1073741830_1006 len=56 Live_repl=3  [DatanodeInfoWithStorage[172.19.0.7:9866,DS-f3a01852-7883-48b1-a195-b8f0ac9e63fe,DISK], DatanodeInfoWithStorage[172.19.0.6:9866,DS-0f405296-7607-49d6-ad9d-54d0f31cde83,DISK], DatanodeInfoWithStorage[172.19.0.8:9866,DS-819df643-f57f-427e-877f-dbcad337fae2,DISK]]


        Status: HEALTHY
         Number of data-nodes:	3
         Number of racks:		1
         Total dirs:			0
         Total symlinks:		0

        Replicated Blocks:
         Total size:	56 B
         Total files:	1
         Total blocks (validated):	1 (avg. block size 56 B)
         Minimally replicated blocks:	1 (100.0 %)
         Over-replicated blocks:	0 (0.0 %)
         Under-replicated blocks:	0 (0.0 %)
         Mis-replicated blocks:		0 (0.0 %)
         Default replication factor:	3
         Average block replication:	3.0
         Missing blocks:		0
         Corrupt blocks:		0
         Missing replicas:		0 (0.0 %)

        Erasure Coded Block Groups:
         Total size:	0 B
         Total files:	0
         Total block groups (validated):	0
         Minimally erasure-coded block groups:	0
         Over-erasure-coded block groups:	0
         Under-erasure-coded block groups:	0
         Unsatisfactory placement block groups:	0
         Average block group size:	0.0
         Missing block groups:		0
         Corrupt block groups:		0
         Missing internal blocks:	0
        FSCK ended at Sat Oct 02 06:26:19 UTC 2021 in 0 milliseconds


        The filesystem under path '/data/text' is HEALTHY

3. Получите информацию по любому блоку из п.2 с помощью "hdfs fsck -blockId”. Обратите внимание на Generation Stamp (GS number).

> hdfs fsck -blockId blk_1073741830
        
        Connecting to namenode via http://namenode:9870/fsck?ugi=root&blockId=blk_1073741830+&path=%2F
        FSCK started by root (auth:SIMPLE) from /172.19.0.5 at Sat Oct 02 06:41:00 UTC 2021

        Block Id: blk_1073741830
        Block belongs to: /data/text
        No. of Expected Replica: 3
        No. of live Replica: 3
        No. of excess Replica: 0
        No. of stale Replica: 0
        No. of decommissioned Replica: 0
        No. of decommissioning Replica: 0
        No. of corrupted Replica: 0
        Block replica on datanode/rack: 1f2c886072ce/default-rack is HEALTHY
        Block replica on datanode/rack: 264a9c83351b/default-rack is HEALTHY
        Block replica on datanode/rack: ee71fae4fcb1/default-rack is HEALTHY

У выбранного блока(blk_1073741830_1006) GS number равен 1006.
