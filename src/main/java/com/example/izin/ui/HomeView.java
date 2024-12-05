/**package com.example.izin.ui;

import com.example.izin.Model.Employee;
import com.example.izin.Services.EmployeeServiceImplementation;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;

@Route("home")
@RolesAllowed("home")
public class HomeView extends VerticalLayout {

    public HomeView(EmployeeServiceImplementation service) {
        var crud = new GridCrud<>(Employee.class , service);
        crud.getGrid().setColumns("name", "lastname", "tckn", "birth_date", "date_of_employment");
        crud.getCrudFormFactory().setVisibleProperties("name", "lastname", "tckn", "birth_date", "date_of_employment");


        // Veritabanındaki verileri görünür hale getirme
        add(
                new H1("Çalışanlar"),
                crud
        );
    }
}
**/