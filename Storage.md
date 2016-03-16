# Storage #

Этот класс предоставляет функции для сохранения данных, полученных в результате выполнения скрипта

Table 1. `com.flinty.book.bagheera.model.IScriptDataStorage`

| Метод | Описание |
|:------|:---------|
| Map getMetadata(String scriptFileName) | считывает метаданные указанного скрипта(обычно используется для хранения введенных пользователем значений: дата начала периода и т.д.) |
| void saveMetadata(String scriptFileName, Map metadata) | сохраняет метаданые указанного скрипта |
| Serializable getData(String scriptFileName) | считывает данные указанного скрипта(обычно используется для хранения истории загруженных новостей) |
| void saveData(String scriptFileName, Serializable data) | сохраняет данные указанного скрипта |
| File saveTempData(String fileName, byte[.md](.md) content) | сохраняет временные данные (обычно используется для кеширования загруженных attachements); после отработки скрипта эти данные стираются |