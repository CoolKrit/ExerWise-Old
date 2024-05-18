package com.example.exerwise.presentation.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.exerwise.R
import com.example.exerwise.data.model.Exercise
import com.example.exerwise.data.model.Workout
import com.example.exerwise.databinding.FragmentEditWorkoutBinding
import com.example.exerwise.presentation.adapter.CreateWorkoutAdapter
import com.example.exerwise.presentation.viewmodel.CreateWorkoutViewModel
import com.example.exerwise.presentation.viewmodel.SharedViewModel

class EditWorkoutFragment : Fragment() {

    private var _binding: FragmentEditWorkoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateWorkoutViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workout = arguments?.getParcelable<Workout>("workout")
        if (workout != null) {
            viewModel.setWorkout(workout)
        }

        binding.workoutName.setText(viewModel.workoutName)

        val recyclerView = binding.exercisesRV
        val adapter = CreateWorkoutAdapter(viewModel) {
            showDialogFragment(it)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        binding.closeEditWorkout.setOnClickListener {
            findNavController().popBackStack()
        }

        // Observe added exercises
        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            if (exercises != null) {
                adapter.submitList(exercises)
            }
        }

        // Observe selected exercise
        sharedViewModel.selectedExercise.observe(viewLifecycleOwner) { exercise ->
            if (exercise != null) {
                viewModel.addExercise(exercise.toDomainModel())
            }
        }

        // Add exercise button
        binding.addExerciseButton.setOnClickListener {
            findNavController().navigate(R.id.action_editWorkoutFragment_to_exerciseListFragment)
        }

        binding.saveEditWorkout.setOnClickListener {
            viewModel.workoutName = binding.workoutName.text.toString()
            if (viewModel.workoutName.isNotEmpty()) {
                viewModel.saveWorkout()
            } else {
                Toast.makeText(requireContext(), "Please enter workout name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedViewModel.clearData()
        _binding = null
    }

    private fun showDialogFragment(exercise: Exercise) {
        val dialogBinding = layoutInflater.inflate(R.layout.fragment_dialog_exercise_details, null)
        val myDialog = Dialog(requireContext())
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        Glide.with(dialogBinding.findViewById<ImageView>(R.id.exerciseIV).context)
            .load(exercise.gifUrl)
            .into(dialogBinding.findViewById(R.id.exerciseIV))
        dialogBinding.findViewById<TextView>(R.id.textViewName).text = exercise.name
        dialogBinding.findViewById<TextView>(R.id.textViewBodyPart).text = "Body part: ${exercise.bodyPart}"
        dialogBinding.findViewById<TextView>(R.id.textViewTarget).text = "Target muscle: ${exercise.target}"
        dialogBinding.findViewById<TextView>(R.id.textViewEquipment).text = "Equipment: ${exercise.equipment}"
        var instructions = ""
        var index = 1
        exercise.instructions.forEach { i ->
            instructions += if (i == exercise.instructions.last()) "${index++}. " + i else "${index++}. " + i + "\n"
        }
        dialogBinding.findViewById<TextView>(R.id.textViewInstructions).text = instructions
    }
}