package com.cookandroid.miniproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskListActivity extends AppCompatActivity {
    EditText editTask;
    Button btnAddTask, btnBack;
    LinearLayout taskContainer;
    String selectedDate;
    ArrayList<String> taskList;
    ArrayList<CheckBox> checkBoxList;
    private static final String FILE_PREFIX = "task_list_";
    private static final String FILE_SUFFIX = ".json";
    private static final String PREFS_NAME = "MyPrefsFile";

    // 체크박스 상태를 저장할 SharedPreferences 키
    private static final String CHECKBOX_PREF_PREFIX = "checkbox_";

    // SharedPreferences 객체
    private SharedPreferences sharedPrefs;

    private boolean inEditMode = false;
    private boolean isNewTask = false;

    private HashMap<String, Boolean> checkBoxStatesBeforeEdit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);

        editTask = (EditText) findViewById(R.id.EditTask);
        btnAddTask = (Button) findViewById(R.id.BtnAddTask);
        btnBack = (Button) findViewById(R.id.BtnBack);
        taskContainer = (LinearLayout) findViewById(R.id.TaskContainer);

        selectedDate = getIntent().getStringExtra("selectedDate");
        setTitle(selectedDate);

        taskList = new ArrayList<>();
        checkBoxList = new ArrayList<>();
        checkBoxStatesBeforeEdit = new HashMap<>();

        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadTasks();

        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task = editTask.getText().toString();
                isNewTask = true;
                if (!task.isEmpty()) {
                    taskList.add(task);
                    addTaskToView(task);
                    saveTasks();
                    editTask.setText("");
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addTaskToView(String task) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setTextSize(15);
        checkBox.setText(task);

        if(!isNewTask) {
            boolean isChecked = sharedPrefs.getBoolean(CHECKBOX_PREF_PREFIX + task, false);
            checkBox.setChecked(isChecked);
        }
        else {
            checkBox.setChecked(false);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 체크박스의 상태 변화에 따라 SharedPreferences에 저장
                sharedPrefs.edit().putBoolean(CHECKBOX_PREF_PREFIX + task, isChecked).apply();
            }
        });

        taskContainer.addView(checkBox);
        checkBoxList.add(checkBox); // 체크박스 목록에 추가
    }

    private void saveTasks() {
        try (FileOutputStream fos = openFileOutput(FILE_PREFIX + selectedDate + FILE_SUFFIX, Context.MODE_PRIVATE)) {
            JSONArray jsonArray = new JSONArray(taskList);
            fos.write(jsonArray.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        taskList.clear();

        try (FileInputStream fis = openFileInput(FILE_PREFIX + selectedDate + FILE_SUFFIX);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                String task = jsonArray.getString(i);
                if (!taskList.contains(task)) {
                    taskList.add(task);
                    addTaskToView(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(!inEditMode) {
            menu.add(0, 1, 0, "Edit");
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                btnAddTask.setEnabled(false);
                saveCheckBoxStatesBeforeEdit();
                btnBack.setVisibility(View.INVISIBLE);
                showEditButtons();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditButtons() {
        inEditMode = true;
        invalidateOptionsMenu();

        for(CheckBox checkBox  : checkBoxList) {
            checkBox.setChecked(false);
        }

        Button btnAllCheck = new Button(this);
        btnAllCheck.setText("Select All");
        btnAllCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(CheckBox checkBox : checkBoxList) {
                    checkBox.setChecked(true);
                }
            }
        });
        taskContainer.addView(btnAllCheck);

        Button btnDelete = new Button(this);
        btnDelete.setText("Delete");
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCheckedTasks();
            }
        });
        taskContainer.addView(btnDelete);

        Button btnFinishEdit = new Button(this);
        btnFinishEdit.setText("Complete");
        btnFinishEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideEditButtons();
                btnAddTask.setEnabled(true);
                btnBack.setVisibility(View.VISIBLE);
                inEditMode = false;
                isNewTask = false;
                restoreCheckBoxStatesAfterEdit();
                invalidateOptionsMenu();
            }
        });
        taskContainer.addView(btnFinishEdit);
    }

    private void hideEditButtons() {
        // 삭제 및 수정 완료 버튼 숨기기
        taskContainer.removeAllViews(); // 모든 뷰 삭제
        // 다시 할 일 목록 불러오기
        for(CheckBox checkBox : checkBoxList) {
            taskContainer.addView(checkBox);
        }
    }

    private void deleteCheckedTasks() {
        // 체크된 항목 삭제
        ArrayList<String> tasksToRemove = new ArrayList<>();
        for (CheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                tasksToRemove.add(checkBox.getText().toString());
            }
        }

        // 실제 데이터에서 삭제 및 화면에서 제거
        for (String task : tasksToRemove) {
            removeFromLayout(task);
            taskList.remove(task);

            removeFromFile(task);

            sharedPrefs.edit().remove(CHECKBOX_PREF_PREFIX + task).apply();

            for (CheckBox checkBox : checkBoxList) {
                if (checkBox.getText().toString().equals(task)) {
                    checkBoxList.remove(checkBox);
                    break;
                }
            }
        }

        saveTasks();
    }

    private void removeFromFile(String taskToRemove) {
        try (FileInputStream fis = openFileInput(FILE_PREFIX + selectedDate + FILE_SUFFIX);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONArray(sb.toString());
            JSONArray newJsonArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                String task = jsonArray.getString(i);
                if (!task.equals(taskToRemove)) {
                    newJsonArray.put(task);
                }
            }

            try (FileOutputStream fos = openFileOutput(FILE_PREFIX + selectedDate + FILE_SUFFIX, Context.MODE_PRIVATE)) {
                fos.write(newJsonArray.toString().getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void removeFromLayout(String task) {
        // 해당 task를 가진 체크박스 제거
        for (int i = 0; i < taskContainer.getChildCount(); i++) {
            View view = taskContainer.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.getText().toString().equals(task)) {
                    taskContainer.removeViewAt(i);
                    break;
                }
            }
        }
    }

    // Edit 모드 진입 전에 체크박스 상태 저장
    private void saveCheckBoxStatesBeforeEdit() {
        checkBoxStatesBeforeEdit.clear(); // 기존 저장 내용 초기화

        for (CheckBox checkBox : checkBoxList) {
            checkBoxStatesBeforeEdit.put(checkBox.getText().toString(), checkBox.isChecked());
        }
    }

    // Edit 모드 종료 후에 체크박스 상태 복원
    private void restoreCheckBoxStatesAfterEdit() {
        for (CheckBox checkBox : checkBoxList) {
            String task = checkBox.getText().toString();
            if (checkBoxStatesBeforeEdit.containsKey(task)) {
                checkBox.setChecked(checkBoxStatesBeforeEdit.get(task));
            }
        }
    }
}

