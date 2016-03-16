# `ForumDecorator` #

В этом приложении описаны функции, добавленные `ForumDecorator.groovy`.

| Функция | Описание |
|:--------|:---------|
| forum(String coverPageURL) | Конструктор. coverPageURL - адрес логотипа ленты, - является опциональным аргументом. Логотип используется в качестве обложки FB2-книги. |
| void useTimeStamp(boolean askForTimeStampValue,boolean confirmTimeStampValue) | Использование временной метки для определения начала периода. askForTimeStampValue - выводить ли диалоговое окно с редактором даты начала периода. Значение по-умолчанию: false . confirmTimeStampValue - выводить ли диалоговое окно для подтверждения обновленного значения временной ветки. Значение по-умолчанию: false . |
| void authorize{->...} | Фукнция задает алгоритм авторизации (детали см. в [Authorization](Authorization.md)) |
| String extractTitle{String content ->...} | Функция задает алгоритм определения заголовка форума из содержимого первой страницы (content). |
| String extractNextPageUrl{String content, int pageNumber ->...} | Функция задает алгоритм определения url следующей страницы форума из содержимого текущей страницы (content) и номера текущей страницы. |