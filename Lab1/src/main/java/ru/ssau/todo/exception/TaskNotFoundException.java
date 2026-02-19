package ru.ssau.todo.exception;

public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException (String message){
        super(message);
    }

}
