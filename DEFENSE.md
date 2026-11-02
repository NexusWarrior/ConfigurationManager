# Шпаргалка для защиты варианта 24

Команды выполняются из корня проекта на macOS/Linux. Откройте этот файл до
переключения коммитов: в ранних версиях его ещё нет. Для каждого этапа выполните
`git switch --detach <хеш>`, затем `./run.sh test` и команды демонстрации.
Сообщение `detached HEAD` при переключении нормально.

## 1. Настройка CLI

```shell
git switch --detach 900aff4aeda142f12494a7e4cb50a4d24b43012c
./run.sh test
./run.sh --package A --repository tests/fixtures/repository.txt \
  --test-mode true --output graph.png --filter Z
```

Показать пять строк `ключ=значение`. Для ошибки изменить, например, `--output`
на `graph.svg`; приложение сообщит, что требуется PNG. Аналогично проверяются
имя пакета, URL/путь, тестовый режим и фильтр (тесты покрывают все параметры).
Сказать: «Все настройки передаются через CLI и проверяются до начала работы».

## 2. Прямые зависимости NuGet

```shell
git switch --detach d29da811fe5bdd3acc650b28c8eed794211c7696
./run.sh test
./run.sh --package Microsoft.Extensions.Http \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false --output graph.png --filter System
```

Нужен интернет. Сказать: «Приложение читает NuGet V3 Service Index, находит
`PackageBaseAddress`, выбирает последнюю стабильную версию, загружает `.nuspec`
и выводит прямые зависимости. Менеджер пакетов для их получения не вызывается».

## 3. Рекурсивный DFS

```shell
git switch --detach 79cce917cf263bf07a39dc34b9d1acd0eef8a6d1
./run.sh test
./run.sh --package A --repository tests/fixtures/cycle.txt \
  --test-mode true --output cycle.png --filter Z
./run.sh --package A --repository tests/fixtures/filter.txt \
  --test-mode true --output filtered.png --filter C
```

В первом запуске виден цикл `A → B → C → A`, но программа завершается. Во втором
`C` исключён из графа. Сказать: «Граф строится рекурсивным DFS; посещённые
пакеты не обходятся повторно, фильтр применяется до рекурсивного перехода».

## 4. Обратные зависимости

```shell
git switch --detach 8e84982b79c7dcfd5e5534d0d40b9e14a2e86065
./run.sh test
./run.sh --package A --repository tests/fixtures/repository.txt \
  --test-mode true --output graph.png --filter Z
```

Ожидаемый вывод:

```text
A <- (none)
B <- A
C <- A, B
```

Сказать: «После DFS для каждого пакета находятся вершины, у которых есть ребро
к нему. Например, от `C` зависят `A` и `B`».

## 5. Graphviz и PNG

```shell
git switch --detach df8550a2f69d37e6ac87989d8a5834ae3eeaf9a1
./run.sh test
mkdir -p build/examples
./run.sh --package A --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/A.png --filter Z
./run.sh --package B --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/B.png --filter Z
./run.sh --package C --repository tests/fixtures/repository.txt \
  --test-mode true --output build/examples/C.png --filter Z
open build/examples/A.png
```

Показать DOT-текст в терминале и три PNG в `build/examples/`. Сказать: «Ребро
направлено от пакета к зависимости. DOT совместим с Graphviz, PNG создаётся
Java2D». Штатный `dotnet package list --include-transitive` работает с
конкретным .NET-проектом и целевой платформой; наше приложение объединяет
группы платформ и выбирает последние стабильные версии, поэтому результаты
могут отличаться. Подробности: `docs/nuget-comparison.md`.

## После защиты

```shell
git switch master
git status
```

На Windows используйте `run.bat` с теми же аргументами. Если понадобится снова
прочитать эту шпаргалку из раннего коммита: `git show master:DEFENSE.md`.
