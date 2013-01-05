

package com.flinty.book.bagheera.script.input.rss

import java.util.regex.Matcher
import java.util.regex.Pattern

import com.flinty.book.bagheera.common.Utils






class RegexpTest extends GroovyTestCase{

	void testRegexp(){
		String.metaClass.getAll  = {String... patterns ->
			if(Utils.isBlank(delegate)){
				return []
			}
			List result = []
			for(String pattern : patterns){
				Pattern p = Pattern.compile(pattern,Pattern.DOTALL)
				Matcher matcher = p.matcher(delegate)
				while(matcher.find()){
					result<<matcher.group(1)
				}
			}
			return result
		}
		
		String testStr = '''

<p>Провод у AH-D7000 - самое уязвимое место этих грандиозных наушников. М??ло того, что он невозможно длинный для мобильного использования (разве что вокруг шеи заматывать в пять витков или пускать вдоль тела - а-ля джанеу брамина) (janeu или yajnyopavitam - священная белая нить, которую носят брамины через левое плечо), так еще и жесткий как сталь из-за нейлоновой оплётки. Как следствие после каждого надевания провод наушников запутывается в упрямую упругую змею, которая буквально вырывается из рук и категорически отказывается компактно скручиваться. Считается, что такая оплётка - высший пилотаж, потому как никогда не износится, но на практике это очень неудобно. </p>

<p>Короче говоря, отправился я в Doctorhead делать обрезание, а за одно - поглазеть на новинки аудиофилии, которые скопились за год моего отсутствия в теме. Пока искали нужный штекер (Neutrik, как водится, хоть и числился по компьютеру, на складе отсутствовал, поэтому остановились на Amphenol'e), пока отмеряли, пока укорачивали, у меня вышло больше часа свободного времени на инспекцию, результатами которой с удовольствием делюсь с читателями. </p>

<p>Первое потрясение - обновление флагмана Denon и выпуск вместо AH-D7000  модели AH-D7100.  Эту модель заслуженно окрестили Music Maniac, поскольку такое название идеально описывает эстетическую катастрофу, которую сотворили японские невменяемые люди. Скажу честно: я даже подумал, что, может, это я один такой привередливый и не в состоянии оценить модных веяний, поэтому сразу по возвращении домой прошерстил сетевые форумы на предмет альтернативных оценок. Поверите ли: я не нашел ни одного отзыва - что на американских, что на русских площадках - в котором бы положительно отзывались о новом дизайне флагманских наушников Denon! Ни одного! Спектр отрицания широк - от яростного возмущения до презрительного плевания, но лейтмотив единодушен - это кошмар:</p>



<p align=center><img src="/upload/sgolub/438_2.jpg" width="550" />

<p>То есть вы понимаете, что они сделали? Поверх благородных деревянных чашек, покрытых таким же рояльным лаком, что и AH-D7000, налепили нашлепки из дешевейшего пластика-серебрянки, который к тому же покрывает еще и всю дужку аж до самого сопряжения с верхним оголовьем! В результате получился монстр, вопиющая эклектичность которого не выдает ничего,  кроме патологической безвкусицы дизайнеров японской фирмы. Хотели urban-style, получили кентавра. Уродство особенно удручающе проявляется в сравнении с классической моделью:</p>



<p align=center><img src="/upload/sgolub/438_3.jpg" width="550" />

<p>Если бы инновация ограничилась эстетическим самоубийством, можно было, наверное, и потерпеть, однако пластик-серебрянка привел еще и к ухудшению конструктивных качеств: исчезла жесткая фиксация на стыке с верхним изголовьем. В моих AH-D7000 крепление очень тугое и акцентированное, в новых AH-D7100 вертикальные положения вообще не фиксируются (по крайней мере на тех наушниках, что находятся на стенде Doctorhead).</p>

<p>Единственный плюс AH-D7100 - амбюшуры стали значительно глубже, так что любые неминиатюрные человеческие  уши (вроде моих) перестали касаться тканевой защиты мембраны. Звучание нового флагмана Denon - слава тебе господи - осталось без изменений, то есть: по-прежнему близко к идеалу (в моем представлении, разумеется).</p>

<p>Кстати, об идеале. Я до такой степени прислушался и прикипел к AH-D7000, что без особых мучений оставил Beyerdynamic T5p дома, расставшись с ними на долгие пять месяцев индийской зимовки. Мирового рекордсмена звуковой детализации (T5p) я использовал после покупки AH-D7000 только для прослушивания классической музыки, однако в последнее время перестал делать и это, поскольку, основательно разогревшись, Денон стал демонстрировать кроме своих уникальных низких частот еще и очень аккуратную прорисовку верхов. До того аккуратную, что нужда в T5p полностью отпала. </p>
'''
	println testStr.getAll('img src="/(upload.*?)"')	

	}
}
