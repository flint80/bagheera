import groovy.lang.Closure

//priority:0
/**
 * Forum Login Helper Class
 *
 */
class LogoutForm {

	String urlString

	Closure url = { String value-> urlString = value }
	Closure confirmLogoutInt = { String content-> return null }
	Closure confirmLogout = { Closure cl-> confirmLogoutInt = cl }
}