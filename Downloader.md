# Downloader #

Этот класс предоставляет функции для загрузки по HTTP-протоколу

Table 1. `com.flinty.book.bagheera.model.standard.StandardDownloader`

| Метод | Описание |
|:------|:---------|
| String loadAsString(java.net.URL url) | получает контент странички в виде строки |
| byte[.md](.md) load(URL url, Map headers = [:]) | получает контент странички в виде массива байтов; на основании полученного ответа заполняет переданную Map парами header.name-header.value |
| String post(URL url, Map params){ | отправляет POST-запрос на указанный URL, передавая в качестве параметров значения, указанные в params; возвращает контент в виде строки |