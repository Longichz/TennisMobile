package xyz.genscode.tennis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import xyz.genscode.tennis.data.Storage
import xyz.genscode.tennis.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity(){

    lateinit var b : ActivityMainMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0); // Устанавливаем нулевую анимацию
        b = ActivityMainMenuBinding.inflate(layoutInflater)

        //Полноэкранный режим
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(b.root)

        //Начинаем игру, убираем все элементы и переходим на игровое активити
        b.btStart.setOnClickListener {
            hideButtons()
            Handler().postDelayed({
                val gameActivity = Intent(this, GameActivity :: class.java)
                startActivity(gameActivity)
            }, 300)
        }

        //Показываем рекорд
        b.btRecord.setOnClickListener {
            hideButtons()
            b.llRecord.visibility = View.VISIBLE

            b.btBack.setOnClickListener {
                b.llRecord.visibility = View.INVISIBLE
                showButtons()
            }

            //Получаем рекорд
            val record = Storage(this).getRecord()
            b.tvScore.text = record
        }

        //Выод
        b.btExit.setOnClickListener {
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        showButtons()
    }

    //Скрыть все эелменты в меню
    fun hideButtons(){
        b.llButtons.isEnabled = false;

        b.tvName.visibility = View.INVISIBLE
        b.llButtons.visibility = View.INVISIBLE
        b.llBlack.visibility = View.INVISIBLE
    }

    //Показать все элементы
    fun showButtons(){
        b.llButtons.isEnabled = true;

        b.tvName.visibility = View.VISIBLE
        b.llButtons.visibility = View.VISIBLE
        b.llBlack.visibility = View.VISIBLE
    }
}