package com.flinty.book.bagheera.common

/**
 * Class representing a mesage that can be shown to user
 */
class Message {

	MessageType type

	String message

	enum MessageType{
		MESSAGE{
			String toString() {
				"Сообщение"
			}
		},
		WARNING{
			String toString() {
				"Предупреждение"
			}
		},
		ERROR{
			String toString() {
				"Ошибка"
			}
		}
	}
}
