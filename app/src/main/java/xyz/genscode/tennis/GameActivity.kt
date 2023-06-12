package xyz.genscode.tennis

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Interpolator
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import xyz.genscode.tennis.data.Storage
import xyz.genscode.tennis.databinding.ActivityGameBinding
import java.util.*


@Suppress("DEPRECATION")
class GameActivity : AppCompatActivity() {
    private val fallingViews = mutableListOf<ImageView>()
    lateinit var b : ActivityGameBinding

    private var score = 0
    private var time = 30
    private var side = true  //true - ракетка на правой стороне экрана, false - левой

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0) // Устанавливаем нулевую анимацию
        b = ActivityGameBinding.inflate(layoutInflater)

        //Полноэкранный режим
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(b.root)

        showGui()
        setRacketControl()

        //Назад
        b.btBack.setOnClickListener {  exit() }

        startLevel()
    }

    fun startLevel(){
        time = 30
        score = 0
        b.tvScore.text = "0"

        //Спавним каждые 2 секунды
        var timeTicks = 0
        var spawnTicks = 0
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                runOnUiThread {

                    if(time <= 0) {  //время вышло, ничего не спавним, показываем очки
                        timer.cancel()
                        Handler().postDelayed({
                            endLevel()
                        }, 3000)
                    }

                    //Показывает счетик времени
                    var timeString = time.toString()
                    if(timeString.length == 1) timeString = "0$timeString"

                    b.tvTime.text = "0:$timeString"

                    spawnTicks++ //отсчет времени для спавна мячей
                    timeTicks++ //отсчет времени для изменения счетчика времени

                    if(timeTicks >= 10){ //изменяем счетчик времени
                        time--
                        timeTicks = 0
                    }

                    if(time in 20 .. 30){ //от 20 до 30 секунды, мячи спавнятся каждые 1600мс
                        if(spawnTicks >= 16){
                            spawnTicks = 0
                            spawnFallingView()
                        }
                    }else if(time in 10..19){ //от 10 до 19 секунды, мячи спавнятся каждые 1000мс
                        if(spawnTicks >= 10){
                            spawnTicks = 0
                            spawnFallingView()
                        }
                    }else if(time in 0 .. 9){ //от 0 до 9 секунды, мячи спавнятся каждые 400мс
                        if(spawnTicks >= 4){
                            spawnTicks = 0
                            spawnFallingView()
                        }
                    }
                }
            }
        }, 2000, 100)
    }

    //Спавним мячики
    private fun spawnFallingView() {
        //Создаем мяч
        val ball = ImageView(this)
        ball.setImageResource(R.drawable.ball)
        ball.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //Делаем рандомное появление по x
        val screenWidth = b.container.width.toFloat()
        val randomX = (70 .. screenWidth.toInt()-70).random().toFloat()

        ball.x = randomX

        //Добавление ImageView в контейнер
        b.container.addView(ball)
        fallingViews.add(ball)

        val startY = 0f //Начальная позиция по вертикали
        val endY = b.container.height.toFloat() //Конечная позиция по вертикали

        val animator = ValueAnimator.ofFloat(startY, endY)
        animator.duration = 2000 //Длительность анимации в миллисекундах
        animator.interpolator = AccelerateInterpolator()
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            ball.translationY = animatedValue

            //Проверка столкновений
            checkCollision(ball)
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                removeFallingView(ball)
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}

        })

        animator.start()
    }

    private fun removeFallingView(ball: ImageView) {
        //Удаление ImageView
        b.container.removeView(ball)
        fallingViews.remove(ball)
        ball.visibility = View.GONE
    }

    //Проверяем столкнулись ли ракетка и мяч
    private fun checkCollision(ball: ImageView) {
        val fallingRect = Rect(
            ball.x.toInt(),
            ball.y.toInt(),
            (ball.x + ball.width).toInt(),
            (ball.y + ball.height).toInt()
        )

        val racketRect = Rect(
            b.racket.x.toInt(),
            b.racket.y.toInt() + b.racket.height/2,
            (b.racket.x + b.racket.width).toInt(),
            (b.racket.y + b.racket.height/2).toInt()
        )

        if (Rect.intersects(fallingRect, racketRect)) { // Произошло столкновение
            ball.x = -500f
            removeFallingView(ball)
            addScore()
        }
    }

    //Добавляем очки
    private fun addScore(){
        if(time in 20 .. 30) score++ //бал за мяч
        if(time in 10 .. 19) score+=2 //два балла за мяч
        if(time in 0 .. 9) score+=3 //три балла за мяч

        b.tvScore.text = score.toString()

        //Анимация отбивания мяча ракеткой
        if(!side){
            b.racketImg.animate().rotationX(-25f).setDuration(150).start()
            b.racketImg.animate().rotationY(-25f).setDuration(150).start()
        }else{
            b.racketImg.animate().rotationX(-25f).setDuration(150).start()
            b.racketImg.animate().rotationY(25f).setDuration(150).start()
        }
        Handler().postDelayed({
            b.racketImg.animate().rotationX(0f).start()
            b.racketImg.animate().rotationY(0f).start()
        },150)
    }

    //Завершаем уровень
    //Завершаем уровень
    //Завершаем уровень
    fun endLevel(){
        //Скрываем все
        hideTopMenu()
        b.blurBackground.visibility = View.VISIBLE
        b.racket.visibility = View.INVISIBLE
        b.blackGradient.visibility = View.INVISIBLE

        //Показываем окно
        b.llEndLevel.visibility = View.VISIBLE

        //Нажали рестарт
        b.btRestart.setOnClickListener {
            showGui()
            startLevel()
        }
        //Нажали назад
        b.btEndLevelBack.setOnClickListener { exit() }

        b.tvEndLevelScore.text = score.toString()

        //Получаем рекорд
        val record = Storage(this).getRecord()
        if(Integer.parseInt(record) < score){ //рекорд побит
            //сохраняем новый рекорд
            Storage(this).setRecord(score.toString())
        }
    }

    //Загружаем гуи
    private fun showGui(){
        b.top.y = -150f
        b.llEndLevel.visibility = View.INVISIBLE
        b.tvLoading.visibility = View.VISIBLE
        Handler().postDelayed({
            b.blackGradient.visibility = View.VISIBLE //затемнение
            b.tvLoading.visibility = View.GONE //убираем текст загрузки
            b.blurBackground.visibility = View.GONE //убираем блюр
            Handler().postDelayed({
                showTopMenu() //показываем очки
                b.racket.visibility = View.VISIBLE //показываем ракетку
            }, 250)
        }, 1000)
    }

    //Загружаем логику управления ракеткой
    @SuppressLint("ClickableViewAccessibility")
    fun setRacketControl(){
        var startX = 0f

        b.racket.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    startX = motionEvent.x
                }

                MotionEvent.ACTION_MOVE -> {
                    b.racket.x += motionEvent.x - startX //двигаем ракетку

                    //Получаем размер экрана
                    val displayMetrics = DisplayMetrics()
                    val windowManager = windowManager
                    windowManager.defaultDisplay.getMetrics(displayMetrics)

                    //Поворачиваем ракетку в зависимости от положения на экране
                    if(b.racket.x + b.racket.width/2 < displayMetrics.widthPixels/2){
                        if(side){
                            side = false
                            rotateRacket(false)
                        }
                    }else{
                        if(!side){
                            side = true
                            rotateRacket(true)
                        }
                    }
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun rotateRacket(isRight: Boolean) {
        val to = if (isRight) 30f else -125f
        b.racketImg.animate().rotation(to).start()
    }

    private fun exit(){
        finish()
        overridePendingTransition(0, 0)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    //Показываем и скрываем верхнее меню
    fun showTopMenu(){
        b.top.animate().y(0f).setDuration(300).start()
    }
    fun hideTopMenu(){
        b.top.animate().y(-150f).start()
    }
}