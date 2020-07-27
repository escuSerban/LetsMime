package com.example.letsmime.screens.game

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.letsmime.R
import com.example.letsmime.databinding.GameFragmentBinding
import kotlinx.android.synthetic.main.game_fragment.*


/**
 * Fragment where the game is played
 */
class GameFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    private lateinit var binding: GameFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.game_fragment,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        binding.gameViewModel = viewModel
        // Specify the current activity as the lifecycle owner of the binding. This is used so that
        // the binding can observe LiveData updates
        binding.lifecycleOwner = this

        // Sets up event listening to navigate the player when the game is finished
        viewModel.eventGameFinish.observe(viewLifecycleOwner, Observer { isFinished ->
            if (isFinished) {
                val currentScore = viewModel.score.value ?: 0
                val action = GameFragmentDirections.actionGameToScore(currentScore)
                findNavController(this).navigate(action)
                viewModel.onGameFinishComplete()
            }
        })

        // Buzzes when triggered with different buzz events
        viewModel.eventBuzz.observe(viewLifecycleOwner, Observer { buzzType ->
            if (buzzType != GameViewModel.BuzzType.NO_BUZZ) {
                buzz(buzzType.pattern)
                // Changing the panic time to be red coloured
                binding.timerText.setTextColor(
                    resources.getColor(
                        R.color.colorAccent,
                        context?.theme
                    )
                )
                viewModel.onBuzzComplete()
                // Returning to the initial color after timer restarts
                if (buzzType == GameViewModel.BuzzType.TIMESUP) binding.timerText.setTextColor(
                    resources.getColor(R.color.grey_text_color, context?.theme)
                )
            }
        })

        binding.correctButton.setOnClickListener {
            viewModel.onCorrect()
            pause_button.visibility = View.VISIBLE
            play_button.visibility = View.GONE
        }

        binding.skipButton.setOnClickListener {
            viewModel.onSkip()
            pause_button.visibility = View.VISIBLE
            play_button.visibility = View.GONE
        }

        binding.pauseButton.setOnClickListener {
            viewModel.onPause()
            pause_button.visibility = View.GONE
            play_button.visibility = View.VISIBLE

        }

        binding.playButton.setOnClickListener {
            viewModel.onResume()
            pause_button.visibility = View.VISIBLE
            play_button.visibility = View.GONE
        }

        return binding.root
    }

    /**
     * Given a pattern, this method makes sure the device buzzes
     */
    private fun buzz(pattern: LongArray) {
        val buzzer = activity?.getSystemService<Vibrator>()
        buzzer?.let {
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                buzzer.vibrate(pattern, -1)
            }
        }
    }
}
