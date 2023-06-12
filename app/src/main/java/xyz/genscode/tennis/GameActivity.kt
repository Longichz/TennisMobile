package xyz.genscode.tennis

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import xyz.genscode.tennis.databinding.ActivityGameBinding
import java.util.*


class GameActivity : AppCompatActivity() {
    private val fallingViews = mutableListOf<ImageView>()
    lateinit var b : ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0); // Устанавливаем нулевую анимацию
        b = ActivityGameBinding.inflate(layoutInflater)

        //Полноэкранный режим
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(b.root)

        loadGui()
        setRacketControl()

        //Назад
        b.btBack.setOnClickListener {  exit() }

        //Спавним каждые 2 секунды
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    spawnFallingView()
                }
            }
        }, 2000, 2000) // Создание нового объекта каждые 2 секунды

    }

    //Спавним мячики
    private fun spawnFallingView() {
        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.ball) // Замените на вашу картинку
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val screenWidth = b.container.width.toFloat()
        val screenHeight = b.container.height.toFloat()

        val randomX = (0 until screenWidth.toInt()).random().toFloat()
        val randomY = (0 until screenHeight.toInt()).random().toFloat()

        imageView.x = randomX
        imageView.y = randomY

        // Добавление ImageView в контейнер
        b.container.addView(imageView)
        fallingViews.add(imageView)

        val startY = 0f // Начальная позиция по вертикали
        val endY = b.container.height.toFloat() // Конечная позиция по вертикали

        val animator = ValueAnimator.ofFloat(startY, endY)
        animator.duration = 3000 // Длительность анимации в миллисекундах

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            imageView.translationY = animatedValue

            // Проверка столкновений
            checkCollision(imageView)
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                removeFallingView(imageView)
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}

        })

        animator.start()
    }

    private fun removeFallingView(imageView: ImageView) {
        // Удаление ImageView
        imageView.animate().alpha(0f).setDuration(50).start()
        Handler().postDelayed({
            b.container.removeView(imageView)
            fallingViews.remove(imageView)
        },50)

    }

    private fun checkCollision(imageView: ImageView) {
        // Проверка столкновений с ракеткой
        if (imageView.y + imageView.height >= b.racket.y) {
            removeFallingView(imageView)
        }
    }

    //Загружаем гуи
    fun loadGui(){
        Handler().postDelayed({
            b.blackGradient.visibility = View.VISIBLE //затемнение
            b.tvLoading.visibility = View.GONE //убираем текст загрузки
            b.blurBackground.visibility = View.GONE //убираем блюр
            Handler().postDelayed({
                b.top.visibility = View.VISIBLE //показываем очки
                b.racket.visibility = View.VISIBLE //показываем ракетку
            }, 250)
        }, 1000)
    }

    //Загружаем логику управления ракеткой
    @SuppressLint("ClickableViewAccessibility")
    fun setRacketControl(){
        var startX = 0f
        var side = true //true - ракетка на правой стороне экрана, false - левой



        b.racket.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    startX = motionEvent.x
                }

                MotionEvent.ACTION_MOVE -> {
                    b.racket.x += motionEvent.x - startX

                    val displayMetrics = DisplayMetrics()
                    val windowManager = windowManager
                    windowManager.defaultDisplay.getMetrics(displayMetrics)

                    if(b.racket.x + b.racket.width/2 < displayMetrics.widthPixels/2){
                        if(side){
                            println("left")
                            side = false
                            rotateRacket(false)
                        }
                    }else{
                        if(!side){
                            println("right")
                            side = true
                            rotateRacket(true)
                        }
                    }
                }

            }

            return@setOnTouchListener true;

        }
    }

    fun rotateRacket(isRight: Boolean) {
        val to = if (isRight) 30f else -125f
        b.racketImg.animate().rotation(to).start()
    }

    fun exit(){
        finish();
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

}