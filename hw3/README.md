# Домашнее задание №3
## Линейная регрессия на Scala

Реализован в виде проекта с использованием библиотек `Breeze` и `scopt`.

Формат запуска для получения прогноза:
```sh
hw3 --trainDataFile data/train_data.csv --trainTargetFile data/train_target.csv \
 --testDataFile data/test_data.csv --predictFile data/target.csv
```

Также можно только обучить модель, для получения коэффициентов:
```sh
hw3 --trainDataFile data/train_data.csv --trainTargetFile data/train_target.csv
```
