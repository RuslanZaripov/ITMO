# Отчет. Вариант 6. Описание переменных в Kotlin

Блок описания переменных в языке Kotlin. \
Каждое описание начинается ключевым словом “var” или “val”, далее идет описание переменной. \
Описание содержит имя переменной, затем двоеточие, затем имя типа. Затем может идти начальное значение. \
Предусмотреть инициализацию только для типа Int числом, выражения рассматривать не требуется. \
Используйте один терминал для всех имен переменных и имен типов. \
Используйте один терминал для ключевого слова var(не три ‘v’, ‘a’, ‘r’). \
Пример: 
```kotlin
var a: Int; val c: Int = 2
```

# Построение грамматики

```
List -> List Stmt | Stmt
Stmt -> Modifier id Type Assignment ;
Modifier -> var | val
Type -> : type
Assignment -> = value | eps
```

# Условные обозначения

- List - список инструкций
- Stmt - инструкция
- Modifier - модификатор var или val
- Type - тип переменной
- Assignment - присваивание значения переменной

# Перестроение грамматики

```
List ->	Stmt List1
List1 -> Stmt List1 | eps
Stmt ->	Modifier id Assignment ;
Modifier ->	var | val
Type -> : type
Assignment -> = value | eps
```

# Построение множеств FIRST и FOLLOW

| **NON-TERMINAL** | **FIRST**     | **FOLLOW**    |
|------------------|---------------|---------------|
| _List_           | var, val      | $             |
| _List1_          | var, val, eps | $             |
| _Stmt_           | var, val      | var, val, eps |
| _Modifier_       | var, val      | id            |
| _Type_           | :             | =             |
| _Assignment_     | =, eps        | ;             |
