import groovy.lang.Closure

//priority:1
/**
 * Autorization module for forums
 *
 */
class Authorization {

	LoginForm loginData

	LogoutForm logoutData

	Closure login = { Closure cl->
		loginData = new LoginForm()
		cl.delegate = loginData
		cl.call()
	}
	Closure logout = { Closure cl->
		logoutData = new LogoutForm()
		cl.delegate = logoutData
		cl.call()
	}

	void authorize(Binding binding){
		if(!loginData){
			return
		}
		String loginPageContent = binding.downloader.post(new URL(loginData.urlString), ["${loginData.loginFieldId}": loginData.loginString, "${loginData.passwordFieldId}": loginData.passwordString])
	}

	void logout(Binding binding){
		if(!logoutData || !loginData){
			return
		}
		String logoutPageContent = binding.downloader.loadAsString(new URL(logoutData.urlString))
		String confirmUrl = logoutData.confirmLogoutInt(logoutPageContent)
		if(confirmUrl){
			logoutPageContent = binding.downloader.loadAsString(new URL(confirmUrl))
		}
	}
}