package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.greact.uikit.Array;
import jstack.greact.uikit.SearchBox;

public class ControlTest {
    record Report(String name, SearchBox form) {}

    record Department(String name, long id) { }
    record Person(String fullName, long id) { }

    void runReport(long personId) {}

    void bar() {
        var departments = new Department[]{};

        var globalReports = Array.of(
            new Report("[PDF] Сводка по человеку", new SearchBox(
                new Cascade<>(
                    new Select<>(departments, Department::id, Department::name),
                    departmentId -> {
                        var persons = new Person[]{};
                        return new Select<>(persons, Person::id, Person::fullName);
                    }
                ),
                personId -> new div() {{
                    new h1("Отчет");
                }}
            ))
        );

        new SearchBox(
            new Select<>(globalReports, Report::form, Report::name),
            form -> new div() {{
                new slot<>(form);
            }}
        );
    }
}
