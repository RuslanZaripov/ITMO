# Отчет

# Вариант 6. Описание переменных в Kotlin

# Построение грамматики

```
List -> List Stmt | Stmt.
Stmt -> Modifier id Type Assignment ;.
Modifier -> var | val.
Type -> : type.
Assignment -> = value | eps.
```

# Перестроение грамматики

```
List ->	Stmt List1
List1 -> Stmt List1 | eps
Stmt ->	Modifier id Assignment ;
Modifier ->	var | val
Type -> : type
Assignment -> = value | eps
```

