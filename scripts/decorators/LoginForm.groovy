import groovy.lang.Closure

//priority:0
/**
 * Forum Login Helper Class
 *
 */
class LoginForm {

	String urlString

	String loginString

	String loginFieldId

	String passwordString

	String passwordFieldId

	Closure url = { String value-> urlString = value }
	Closure login = { String value-> loginString = value }
	Closure loginField = { String value-> loginFieldId = value }
	Closure password = { String value-> passwordString = value }
	Closure passwordField = { String value-> passwordFieldId = value }
}