# Сценарий поэтапной защиты

В этом файле собраны команды для демонстрации практической работы по варианту
24. Все команды ниже выполняются из корня проекта.

## Подготовка

Перед защитой открыть терминал в каталоге проекта и проверить состояние Git:

```shell
git status
git log --oneline --reverse
```

Рабочее дерево должно быть чистым. На macOS и Linux скрипт запуска должен быть
исполняемым:

```shell
chmod +x run.sh
```

При первом запуске `run.sh` может загрузить Gradle 9.6.0, поэтому потребуется
доступ в интернет. Загруженные файлы сохраняются в игнорируемом каталоге
`.gradle-bootstrap`.

## Как переключаться между этапами

Для перехода к этапу используется полный хеш его коммита:

```shell
git switch --detach <хеш-коммита>
```

Сообщение о состоянии `detached HEAD` является нормальным: во время защиты
нужно только запускать готовый код, а не создавать новые коммиты.

После демонстрации всех этапов вернуться к итоговой версии:

```shell
git switch master
```

Файл `DEFENSE.md` добавлен после этапных коммитов, поэтому при переключении на
ранний этап он исчезнет из рабочей папки. Руководство можно заранее оставить
открытым или прочитать из любого этапа командой:

```shell
git show master:DEFENSE.md | less
```

## Этап 1. Конфигурация CLI

Коммит:

```text
900aff4aeda142f12494a7e4cb50a4d24b43012c
```

Переключение и тесты:

```shell
git switch --detach 900aff4aeda142f12494a7e4cb50a4d24b43012c
./run.sh test
```

Корректный запуск показывает все пять параметров в формате `ключ=значение`:

```shell
./run.sh \
  --package Newtonsoft.Json \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false \
  --output graph.png \
  --filter System
```

Демонстрация обработки ошибок каждого параметра:

```shell
# Некорректное имя пакета
./run.sh --package "bad package" \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false --output graph.png --filter System

# Некорректный адрес репозитория
./run.sh --package Newtonsoft.Json \
  --repository ftp://example.org/packages \
  --test-mode false --output graph.png --filter System

# Некорректный логический режим
./run.sh --package Newtonsoft.Json \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode maybe --output graph.png --filter System

# Выходной файл имеет неверное расширение
./run.sh --package Newtonsoft.Json \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false --output graph.svg --filter System

# Пустая подстрока фильтра
./run.sh --package Newtonsoft.Json \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false --output graph.png --filter ""
```

Также можно показать неизвестную опцию или отсутствие обязательного параметра:

```shell
./run.sh --unknown value
./run.sh --package Newtonsoft.Json
```

## Этап 2. Прямые зависимости NuGet

Коммит:

```text
d29da811fe5bdd3acc650b28c8eed794211c7696
```

Переключение и тесты:

```shell
git switch --detach d29da811fe5bdd3acc650b28c8eed794211c7696
./run.sh test
```

Получение прямых зависимостей реального NuGet-пакета:

```shell
./run.sh \
  --package Microsoft.Extensions.Http \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false \
  --output graph.png \
  --filter System
```

Что объяснить преподавателю:

- приложение само читает NuGet V3 Service Index;
- из Service Index извлекается ресурс `PackageBaseAddress`;
- выбирается последняя стабильная версия пакета;
- загружается `.nuspec`, из которого читаются прямые зависимости;
- менеджеры пакетов и библиотеки получения зависимостей не используются.

Запрос к NuGet иногда выполняется несколько десятков секунд. Это зависит от
сети и не является зависанием приложения.

## Этап 3. Рекурсивный DFS

Коммит:

```text
79cce917cf263bf07a39dc34b9d1acd0eef8a6d1
```

Переключение и тесты:

```shell
git switch --detach 79cce917cf263bf07a39dc34b9d1acd0eef8a6d1
./run.sh test
```

Обычный тестовый граф:

```shell
./run.sh \
  --package A \
  --repository tests/fixtures/repository.txt \
  --test-mode true \
  --output graph.png \
  --filter Z
```

Граф с циклом `A -> B -> C -> A`:

```shell
./run.sh \
  --package A \
  --repository tests/fixtures/cycle.txt \
  --test-mode true \
  --output cycle.png \
  --filter Z
```

Фильтрация пакета `C` и его ветви:

```shell
./run.sh \
  --package A \
  --repository tests/fixtures/filter.txt \
  --test-mode true \
  --output filtered.png \
  --filter C
```

Что объяснить преподавателю:

- обход реализован рекурсивным DFS;
- множество посещённых пакетов предотвращает бесконечную рекурсию на циклах;
- фильтр применяется без учёта регистра до рекурсивного перехода;
- формат тестового файла: `A: B C`, имена пакетов — заглавные латинские буквы.

## Этап 4. Обратные зависимости

Коммит:

```text
8e84982b79c7dcfd5e5534d0d40b9e14a2e86065
```

Переключение и тесты:

```shell
git switch --detach 8e84982b79c7dcfd5e5534d0d40b9e14a2e86065
./run.sh test
```

Обычный граф:

```shell
./run.sh \
  --package A \
  --repository tests/fixtures/repository.txt \
  --test-mode true \
  --output graph.png \
  --filter Z
```

Ожидаемый результат:

```text
A <- (none)
B <- A
C <- A, B
```

Циклический граф:

```shell
./run.sh \
  --package A \
  --repository tests/fixtures/cycle.txt \
  --test-mode true \
  --output cycle.png \
  --filter Z
```

Ожидаемый результат:

```text
A <- C
B <- A
C <- B
```

Обратные зависимости получаются инвертированием рёбер графа, который был
построен рекурсивным DFS на предыдущем этапе.

## Этап 5. Graphviz и PNG

Коммит:

```text
df8550a2f69d37e6ac87989d8a5834ae3eeaf9a1
```

Переключение и тесты:

```shell
git switch --detach df8550a2f69d37e6ac87989d8a5834ae3eeaf9a1
./run.sh test
mkdir -p build/examples
```

Три примера для разных пакетов:

```shell
./run.sh --package A --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/A.png --filter Z

./run.sh --package B --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/B.png --filter Z

./run.sh --package C --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/C.png --filter Z
```

Проверка созданных изображений на macOS:

```shell
file build/examples/A.png build/examples/B.png build/examples/C.png
open build/examples/A.png
```

Что объяснить преподавателю:

- DOT-текст Graphviz выводится в терминал;
- каждое ребро имеет направление от пакета к его зависимости;
- изолированные вершины также попадают в DOT;
- PNG создаётся средствами Java2D без внешней библиотеки визуализации;
- PNG и остальные бинарные результаты игнорируются Git.

Сравнение со штатным инструментом описано в
`docs/nuget-comparison.md`. Основная команда .NET 10:

```shell
dotnet package list --project Sample.csproj --include-transitive
```

Эта команда является шаблоном: вместо `Sample.csproj` нужно указать путь к
заранее подготовленному .NET-проекту с исследуемым пакетом.

Разработанное приложение объединяет зависимости всех целевых платформ и
выбирает последние стабильные версии. Штатный NuGet работает с конкретным
проектом и целевой платформой, а также применяет правила разрешения конфликтов
версий. Поэтому наборы пакетов и выбранные версии могут отличаться.

## Завершение защиты

Вернуться на основную ветку и убедиться, что рабочее дерево чистое:

```shell
git switch master
git status
```

Чтобы снова увидеть краткую историю этапов:

```shell
git log --oneline --reverse
```

На Windows вместо `./run.sh` используется `run.bat` с теми же аргументами.
