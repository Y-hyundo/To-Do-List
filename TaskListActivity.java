package com.cookandroid.miniproject;

// 필요한 라이브러리와 클래스들을 임포트합니다.
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

// TaskListActivity 클래스는 할 일 목록을 관리하는 화면을 담당합니다.
public class TaskListActivity extends AppCompatActivity {
    // UI 요소와 변수들을 선언합니다.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Activity가 생성될 때 호출됩니다.
        super.onCreate(savedInstanceState);
        // task_list 레이아웃을 이 Activity의 사용자 인터페이스로 설정합니다.
        setContentView(R.layout.task_list);

        // task_list 레이아웃에서 각 뷰의 ID를 찾아 변수에 할당합니다.
        editTask = (EditText) findViewById(R.id.EditTask);
        btnAddTask = (Button) findViewById(R.id.BtnAddTask);
        btnBack = (Button) findViewById(R.id.BtnBack);
        taskContainer = (LinearLayout) findViewById(R.id.TaskContainer);

        // 전달받은 날짜를 가져와서 제목으로 설정합니다.
        selectedDate = getIntent().getStringExtra("selectedDate");
        setTitle(selectedDate);

        // 할 일 목록과 체크박스 목록을 초기화합니다.
        taskList = new ArrayList<>();
        checkBoxList = new ArrayList<>();
        checkBoxStatesBeforeEdit = new HashMap<>();

        // SharedPreferences 객체를 초기화합니다.
        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 저장된 할 일 목록을 불러옵니다.
        loadTasks();

        // 할 일 추가 버튼에 클릭 리스너를 설정합니다.
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

        // 뒤로 가기 버튼에 클릭 리스너를 설정합니다.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // 새로운 할 일을 뷰에 추가하는 메서드입니다.
    private void addTaskToView(String task) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setTextSize(15);
        checkBox.setText(task);

        // 새로운 할 일인지 확인하여 체크박스 상태를 설정합니다.
        if (!isNewTask) {
            boolean isChecked = sharedPrefs.getBoolean(CHECKBOX_PREF_PREFIX + task, false);
            checkBox.setChecked(isChecked);
        } else {
            checkBox.setChecked(false);
        }

        // 체크박스의 상태 변화에 따라 SharedPreferences에 저장합니다.
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPrefs.edit().putBoolean(CHECKBOX_PREF_PREFIX + task, isChecked).apply();
            }
        });

        // 체크박스를 컨테이너에 추가하고 리스트에 추가합니다.
        taskContainer.addView(checkBox);
        checkBoxList.add(checkBox);
    }

    // 할 일 목록을 저장하는 메서드입니다.
    private void saveTasks() {
        try (FileOutputStream fos = openFileOutput(FILE_PREFIX + selectedDate + FILE_SUFFIX, Context.MODE_PRIVATE)) {
            JSONArray jsonArray = new JSONArray(taskList);
            fos.write(jsonArray.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 저장된 할 일 목록을 불러오는 메서드입니다.
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (!inEditMode) {
            menu.add(0, 1, 0, "Edit");
        }
        return true;
    }

    @Override
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

    // 수정 모드에서 버튼들을 표시하는 메서드입니다.
    private void showEditButtons() {
        inEditMode = true;
        invalidateOptionsMenu();

        // 모든 체크박스를 체크 해제합니다.
        for (CheckBox checkBox : checkBoxList) {
            checkBox.setChecked(false);
        }

        // 모두 선택 버튼을 추가합니다.
        Button btnAllCheck = new Button(this);
        btnAllCheck.setText("Select All");
        btnAllCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CheckBox checkBox : checkBoxList) {
                    checkBox.setChecked(true);
                }
            }
        });
        taskContainer.addView(btnAllCheck);

        // 삭제 버튼을 추가합니다.
        Button btnDelete = new Button(this);
        btnDelete.setText("Delete");
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCheckedTasks();
            }
        });
        taskContainer.addView(btnDelete);

        // 수정 완료 버튼을 추가합니다.
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

    // 수정 모드에서 버튼들을 숨기는 메서드입니다.
    private void hideEditButtons() {
        taskContainer.removeAllViews(); // 모든 뷰 삭제
        // 다시 할 일 목록을 불러옵니다.
        for (CheckBox checkBox : checkBoxList) {
            taskContainer.addView(checkBox);
        }
    }

    // 체크된 할 일을 삭제하는 메서드입니다.
    private void deleteCheckedTasks() {
        ArrayList<String> tasksToRemove = new ArrayList<>();
        for (CheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                tasksToRemove.add(checkBox.getText().toString());
            }
        }

        // 실제 데이터에서 삭제 및 화면에서 제거합니다.
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

    // 파일에서 할 일을 삭제하는 메서드입니다.
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

    // 레이아웃에서 체크박스를 제거하는 메서드입니다.
    private void removeFromLayout(String task) {
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

    // 수정 모드 진입 전에 체크박스 상태를 저장하는 메서드입니다.
    private void saveCheckBoxStatesBeforeEdit() {
        checkBoxStatesBeforeEdit.clear(); // 기존 저장 내용 초기화

        for (CheckBox checkBox : checkBoxList) {
            checkBoxStatesBeforeEdit.put(checkBox.getText().toString(), checkBox.isChecked());
        }
    }

    // 수정 모드 종료 후에 체크박스 상태를 복원하는 메서드입니다.
    private void restoreCheckBoxStatesAfterEdit() {
        for (CheckBox checkBox : checkBoxList) {
            String task = checkBox.getText().toString();
            if (checkBoxStatesBeforeEdit.containsKey(task)) {
                checkBox.setChecked(checkBoxStatesBeforeEdit.get(task));
            }
        }
    }
}
