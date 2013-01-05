//@ description Bash

showMessage 'готовим книжку - ждите...'
rss('http://bash.org.ru/rss/', 'http://s.bash.org.ru/logo.gif'){
	usePeriod(true)
}
showMessage 'книжка успешно создана'

