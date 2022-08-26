package info.kgeorgiy.ja.zaripov.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {

    private static final String EMPTY_STRING = "";
    private static final Comparator<Student> FULL_NAME_COMPARATOR = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            .thenComparing(Student::getId);
    private static final Comparator<Student> ID_COMPARATOR = Comparator.comparingInt(Student::getId);
    private static final Comparator<Student> FIRST_NAME_COMPARATOR = Comparator.comparing(Student::getFirstName);

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return map(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return map(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return map(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return map(students, student -> String.join(" ", student.getFirstName(), student.getLastName()));
    }

    private <T> List<T> map(final List<Student> students, final Function<Student, T> mapper) {
        return students.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .sorted(FIRST_NAME_COMPARATOR)
                .map(Student::getFirstName)
                .collect(Collectors.toSet());
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(ID_COMPARATOR)
                .map(Student::getFirstName)
                .orElse(EMPTY_STRING);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, ID_COMPARATOR);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, FULL_NAME_COMPARATOR);
    }

    private List<Student> sortStudentsBy(final Collection<Student> students, final Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(students, student -> student.getGroup().equals(group));
    }

    private List<Student> findStudentsBy(final Collection<Student> students, final Predicate<Student> predicate) {
        return students.stream()
                .filter(predicate)
                .sorted(FULL_NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)
                ));
    }
}
