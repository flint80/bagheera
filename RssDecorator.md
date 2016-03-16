# `RssDecorator` #

В этом приложении описаны функции, добавленные `RssDecorator.groovy`

| Функция | Описание |
|:--------|:---------|
| rss(String forumURL, String coverPageURL) | Конструктор. forumURL - адрес новостной ленты, -является обязательным аргументом. coverPageURL - адрес логотипа ленты, - является опциональным аргументом. Логотип используется в качестве обложки FB2-книги. |
| useTimeStamp(boolean askForTimeStampValue,boolean confirmTimeStampValue) | Использование временной метки для определения начала периода. askForTimeStampValue - выводить ли диалоговое окно с редактором даты начала периода. Значение по-умолчанию: false . confirmTimeStampValue - выводить ли диалоговое окно для подтверждения обновленного значения временной ветки. Значение по-умолчанию: false . |
| usePeriod(boolean askForPeriodValue, int defaultRetrievePeriod) | Получение данных за определенный период времени. askForPeriodValue - выводить ли диалоговое окно с редактором продолжительности периода.Значение по-умолчанию: false . defaultRetrievePeriod - значение продолжительности периода в днях, если askForPeriod=false . Значение по-умолчанию: 14 . |
| updateItem{HTMLSection section->...} | Постобработка собщений. Используя эту функцию, Вы сможете изменить содержимое сообщения (типа [HTMLSection](HTMLSection.md)). |
| buildFileName{Binding binding ->...} | Используйте эту функцию для переопределения алгоритма генерации имени файла. В качестве аргумента в эту функцию передается [Контекст](Context.md) выполнения скрипта. |