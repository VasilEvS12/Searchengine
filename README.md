# Проект локального поискового дивжка по сайтам

Проект с подключенными библиотеками лемматизаторами.
Содержит несколько контроллеров, сервисов и репозиториев
с подключением к бд MySQL.


## Структура проекта

Веб приложение состоит из 2-х частей клиентской и серверной
Клиентская 
В проекте используются 3 слоя:

* **Presentation** -  Презентационный слой представлен классами контроллерами. Контроллеры ожидают запросы по API и отдают ответы
  ответы.
* **Business** - - бизнес логика
  слое
* **Data Access** - слой отвечает за хранение данных, подключение к БД У нас это классы Репозитории.

![img.png](docs/arch.png)

Каждый слой занимается только своими задачами и работа одного слоя не должна перетекать в другой. Например, Контроллер
должен только получать данные от пользователя и вызывать нужный сервис, не более. Все расчеты и проверки должны быть в
классах сервисах.

Если посмотреть слои данного приложения, то увидим не только сами классы, но еще и интерфейсы между ними.

![img.png](docs/currentProject.png)

Интерфейсы нужны, чтобы слои приложения не зависели от реализаций классов. Каждый из классов LemmaController и
MorphologyServiceImpl зависят от интерфейса MorphologyService. Это значит, что сервисы и контроллеры могут меняться
независимо, заменяться и это не будет влиять на другие слои.

Также это относится и к передаваемым данным, у каждого слоя
может быть свои структуры для хранения данных, например Репозитории отдают Entity классы, а сервисы уже упаковывают в
нужные объекты, снова для того чтобы конечный потребитель данных не зависел напрямую от структуры данных в БД. БД может
измениться, поменять структуру таблиц - и это должно произойти максимально незаметно для других слоев.

Если еще раз посмотреть на схему, самой реализации Data Access слоя нет, у нас есть только интерфейс репозитория, а
реализация будет сгенерирована при запуске проекта.

Рекомендации для чтения про архитектуру:

* [Многоуровневая архитектура в проекте на Java (Часть 1)](https://alexkosarev.name/2018/07/27/n-tier-java-part1/)
* [Многоуровневая архитектура в проекте на Java (Часть 2)](https://alexkosarev.name/2018/07/27/n-tier-java-part2/)
* [Чистая архитектура](https://habr.com/ru/post/269589/)

## Исследуйте исходный код проекта

К классам и методам в проекте содержаться комментарии объясняющие особенности поведения, использование аннотаций и
синтаксис. Пройдитесь по всем классам и изучите проект.

## Проверка API

Для проверки API данного проекта вы можете использовать разные инструменты:

* [cURL](https://curl.se/) - консольная утилита.
* [Postman](https://www.postman.com/) - приложения для отправки запросов и тестирования API
* [Insomnia](https://insomnia.rest/) - еще одно приложения для тестирования API

Ниже приведены запросов в формате cURL, вы можете данные из запросов перенести в любое другое приложение.

* **Получение информации о слове**

```bash
curl -X POST http://localhost:8080/api/lemma/info  -i -d '{"word":"брат"}' -H 'Content-Type: application/json'
```

Успешный ответ:

```json
[
  "брат|A С мр,ед,им"
]
```

* **Сохранение слова в БД**

```bash
curl -X POST http://localhost:8080/api/lemma/save -i -d '{"word":"сестра"}' -H 'Content-Type: application/json'
```

Успешный ответ:

```json
{
  "word": "сестра",
  "morphInfo": [
    "сестра|G С жр,ед,им"
  ]
}
```

Слово не найдено с словаре:

```json
{
  "message": "Word <cecnhf> not contains in dictionary!"
}
```

* **Поиск слова в БД**

```bash
curl  -X POST http://localhost:8080/api/lemma/search -i -d '{"query":"бр", "limit":100}' -H 'Content-Type: application/json'
```

Успешный ответ:

```json
{
  "count": 2,
  "words": [
    {
      "word": "брат",
      "morphInfo": "[брат|A С мр,ед,им]",
      "count": 0
    },
    {
      "word": "броненосец",
      "morphInfo": "[броненосец|A С мр,ед,им]",
      "count": 0
    }
  ]
}
```