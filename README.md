# Configuration Manager

CLI-приложение на Kotlin для анализа и визуализации графа зависимостей
пакетов NuGet. Реализуется по варианту 24 практической работы №2.

## Этап 1: конфигурация

На первом этапе приложение принимает и валидирует пять параметров, после
чего выводит их в формате `ключ=значение`:

- `--package` — идентификатор анализируемого NuGet-пакета;
- `--repository` — HTTP(S)-адрес NuGet-репозитория либо путь к тестовому
  файлу;
- `--test-mode` — режим тестового репозитория (`true` или `false`);
- `--output` — путь к будущему PNG-изображению графа;
- `--filter` — подстрока для исключения пакетов на следующих этапах.

Ошибки конфигурации выводятся в `stderr`, после чего приложение завершается
с кодом `2`. Обрабатываются отсутствующие, пустые, неизвестные и повторяющиеся
опции, некорректные значения каждого параметра и недоступные пути.

## Требования

- JDK 25;
- `curl` и `unzip`, если Gradle не установлен локально.

Скрипты запуска используют установленный Gradle либо загружают Gradle 9.6.0
в игнорируемый каталог `.gradle-bootstrap`.

## Сборка и тесты

Linux и macOS:

```shell
./run.sh test
```

Windows:

```bat
run.bat test
```

## Примеры запуска

Работа с NuGet API:

```shell
./run.sh \
  --package Newtonsoft.Json \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false \
  --output graph.png \
  --filter System
```

Работа с тестовым репозиторием:

```shell
./run.sh \
  --package A \
  --repository tests/fixtures/repository.txt \
  --test-mode true \
  --output test-graph.png \
  --filter Z
```

Пример ошибки:

```shell
./run.sh \
  --package "invalid package" \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false \
  --output graph.svg \
  --filter System
```

Для получения краткой справки:

```shell
./run.sh --help
```
