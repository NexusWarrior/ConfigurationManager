# Configuration Manager

CLI-приложение на Kotlin для анализа и визуализации графа зависимостей
пакетов NuGet. Реализуется по варианту 24 практической работы №2.

## Реализованные этапы

### Этап 1: конфигурация

Приложение принимает и валидирует пять параметров:

- `--package` — идентификатор анализируемого NuGet-пакета;
- `--repository` — HTTP(S)-адрес NuGet-репозитория либо путь к тестовому
  файлу;
- `--test-mode` — режим тестового репозитория (`true` или `false`);
- `--output` — путь к будущему PNG-изображению графа;
- `--filter` — подстрока для исключения пакетов на следующих этапах.

Ошибки конфигурации выводятся в `stderr`, после чего приложение завершается
с кодом `2`. Обрабатываются отсутствующие, пустые, неизвестные и повторяющиеся
опции, некорректные значения каждого параметра и недоступные пути.

### Этап 2: прямые зависимости NuGet

Приложение самостоятельно обращается к NuGet V3 API, находит ресурс
`PackageBaseAddress`, выбирает последнюю стабильную версию пакета и загружает
его `.nuspec`. Из XML-манифеста извлекаются прямые зависимости, диапазоны
версий и целевые платформы. Менеджеры пакетов и библиотеки получения
зависимостей не используются.

### Этап 3: граф зависимостей

Транзитивный граф строится рекурсивным обходом DFS. Множество посещённых
пакетов предотвращает бесконечный обход циклов. Пакеты, имя которых содержит
значение `--filter` без учёта регистра, исключаются из графа.

В тестовом режиме репозиторий читается из текстового файла. Каждая непустая
строка имеет вид `A: B C`; имена пакетов — одиночные заглавные латинские буквы,
все упомянутые пакеты должны быть объявлены отдельной строкой.

### Этап 4: обратные зависимости

После рекурсивного обхода рёбра полученного графа инвертируются. Для каждого
пакета выводятся его прямые обратные зависимости — пакеты исследованного графа,
которые от него зависят. Пустое множество обозначается как `(none)`.

### Этап 5: Graphviz и PNG

Приложение выводит текст графа на языке DOT (Graphviz) и сохраняет его PNG-копию
по пути `--output`. PNG строится средствами Java2D, поэтому установка Graphviz
не требуется. Сгенерированные изображения игнорируются Git как бинарные файлы.

Сравнение со штатной командой `dotnet package list --include-transitive` и
объяснение различий приведены в [отдельном документе](docs/nuget-comparison.md).

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

Получение прямых зависимостей через NuGet API:

```shell
./run.sh \
  --package Newtonsoft.Json \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false \
  --output graph.png \
  --filter System
```

Визуализация графа с циклом:

```shell
./run.sh \
  --package A \
  --repository tests/fixtures/cycle.txt \
  --test-mode true \
  --output cycle.png \
  --filter Z
```

Проверка фильтрации пакета `C` и его ветви:

```shell
./run.sh \
  --package A \
  --repository tests/fixtures/filter.txt \
  --test-mode true \
  --output filtered.png \
  --filter C
```

Три примера визуализации разных пакетов из тестового репозитория:

```shell
mkdir -p build/examples
./run.sh --package A --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/A.png --filter Z
./run.sh --package B --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/B.png --filter Z
./run.sh --package C --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/C.png --filter Z
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
