# `LoggerDecorator` #

В этом приложении описаны функции, добавленные `LoggerDecorator.groovy`. Он добавляет несколько полезных методов для логирования. Все логи пишутся в файл logs/script.log.

| Метод | Описание |
|:------|:---------|
| void logError(String error, Throwable t = null) | Добавляет в лог сообщение об ошибке. Если в качестве аргумента передается сама ошибка, то в лог пишется и stackTrace. |
| void logWarning(String warning, Throwable t = null) | Добавляет в лог предупреждение. Если в качестве аргумента передается сама ошибка, то в лог пишется и stackTrace. |
| void logDebug(String messsage) | Добавляет в лог сообщение. |
| void log(String messsage) | То же, что и logDebug. |