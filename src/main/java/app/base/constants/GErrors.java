package app.base.constants;

import app.base.objects.IGEnum;

public enum GErrors implements IGEnum {

    INTERNAL_SERVER_ERROR("Внутренняя ошибка сервера"),
    NOT_FOUND("Не найдено"),
    NOT_ALLOWED("Запрещено"),
    BAD_REQUEST("Неверно заполнены данные"),
    ALREADY_EXISTS("Уже существует"),
    UNAUTHORIZED("Вы не авторизованы"),
    BAD_CREDENTIALS("Неверные данные авторизации"),
    TOKEN_EXPIRED("Токен устарел"),
    INTERNAL_ERROR("Системная ошибка"),
    FIELDS_INVALID_FORMAT("Неправильный формат полей"),
    FIELD_REQUIRED("Требуется заполнить поле"),
    OK("Успешно"),
    CREATED("Создано"),
    ;

    private String message;

    private GErrors(String message) {
        this.message = message;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getValue() {
        return message;
    }
}
