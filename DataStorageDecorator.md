# `DataStorageDecorator` #

В этом приложении описаны функции, добавленные `DataStorageDecorator.groovy`. Он добавляет несколько полезных методов, для сохранения данных и диалога с пользователем.

| Метод | Описание |
|:------|:---------|
| void saveScriptMetadata(Map data) | Вызывает saveMetadata из [Storage](Storage.md), используя в качестве fileName имя файла с выполняемым скриптом. |
| Map getScriptMetadata() | Вызывает getMetadata из [Storage](Storage.md), используя в качестве fileName имя файла с выполняемым скриптом. |
| void saveScriptData(String dataId, Object obj) | Вызывает saveData из [Storage](Storage.md), используя в качестве fileName имя файла с выполняемым скриптом. |
| Object getScriptData(String dataId) | Вызывает getData из [Storage](Storage.md), используя в качестве fileName имя файла с выполняемым скриптом. |