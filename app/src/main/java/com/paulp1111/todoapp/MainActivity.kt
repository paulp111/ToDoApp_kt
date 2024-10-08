package com.paulp111.todoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.paulp111.todoapp.databinding.ActivityMainBinding
import com.paulp111.todoapp.databinding.DialogAddListBinding
import com.paulp1111.todoapp.getParcelableExtraProvider


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var todoListAdapter: TodoListAdapter
    private val todoLists = mutableListOf<TodoList>()
    private lateinit var listActivityLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialsiere den Adapter und setze ihn af den RecyclerView
        todoListAdapter = TodoListAdapter(todoLists,
            { todoList: TodoList ->
                val intent = Intent(this, SingleListViewActivity::class.java).apply {
                    putExtra("todoList", todoList) // übertrage die TodoList über den Intent
                }
                listActivityLauncher.launch(intent)

            },
            { todoList: TodoList, position ->
                showDeleteConfirmationDialog(todoList, position)
            }
        )
        binding.addListButton.setOnClickListener {
            addNewList()
        }


        binding.recyclerViewList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = todoListAdapter
        }
        //Filled list
        val exampleList = TodoList(
            "Random List",
            mutableListOf(
                Item("Test1", false),
                Item("Test2", true),
                Item("Test3", true)
            )
        )
        val exampleList2 = TodoList(
            "list231313",
            mutableListOf(
                Item("Test4", false)
            )
        )

        todoListAdapter.addTodoList(exampleList)
        todoListAdapter.addTodoList(exampleList2)


        listActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()

        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedTodoList =
                    result.data?.getParcelableExtraProvider<TodoList>("updatedTodoList")
                updatedTodoList?.let { updatedList ->
                    val index = todoLists.indexOfFirst { it.title == updatedList.title }
                    if (index != -1) {
                        todoLists[index] = updatedList
                        todoListAdapter.notifyItemChanged(index)
                    }
                }
            }
        }
    }

    // NEUE LISTE
    fun addNewList() {
        val inflater = layoutInflater
        val dialogBinding = DialogAddListBinding.inflate(inflater)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Neue Liste hinzufügen")
        builder.setView(dialogBinding.root)
        builder.setPositiveButton("Hinzufügen", null)
        builder.setNegativeButton("Abbrechen", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setOnClickListener {
                val listName = dialogBinding.listNameInput.text.toString().trim()
                if (listName.isNotEmpty()) {
                    val newList = TodoList(listName, mutableListOf())
                    todoListAdapter.addTodoList(newList)
                    dialog.dismiss()
                } else {
                    dialogBinding.errorMessage.visibility = View.VISIBLE
                    dialogBinding.listNameInput.requestFocus()
                }
            }
            negativeButton.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    // DELETE AUFGABE
    fun showDeleteConfirmationDialog(todoList: TodoList, position: Int) {
        // AlertDialog um  Löschen zu bestätigen
        val dialog = AlertDialog.Builder(this).create()
        dialog.setTitle("Delete List") // Setzt Dialog Title
        dialog.setMessage("List \"${todoList.title}\" will be deleted. Continue?") // Confirm msg for user

        // yes-button um die Liste zu löschen
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { _, _ ->
            // Entfernt die liste aus der todoLists (anhand der Position)
            todoLists.removeAt(position)
            // aktualisiert adapter um die gelöschte Liste im RecyclerView zu entfernen
            todoListAdapter.notifyItemRemoved(position)
            // msg das Liste erfolgreich gelöscht wurde
            Toast.makeText(
                this,
                "List \"${todoList.title}\" deleted successfully",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Cancel-btn, falls abbruch
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
            // Zeigt an das abgebrochen wurde
            Toast.makeText(this, "Deletion cancelled", Toast.LENGTH_SHORT).show()
        }

        // listener wird aufgerufen wenn Dialog angezeigt wird
        dialog.setOnShowListener {
            // zusätzliche funktionen für Yes-btn
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.let { positiveButton ->
                positiveButton.setOnClickListener {
                    // Logik
                    dialog.dismiss() // close after confirm
                    Toast.makeText(this, "Deletion successful", Toast.LENGTH_SHORT)
                        .show() // confirms again
                }
            }
        }
        dialog.show() //shows dialog
    }
}