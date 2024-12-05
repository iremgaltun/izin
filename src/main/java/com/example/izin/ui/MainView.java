package com.example.izin.ui;

import com.example.izin.Model.Employee;
import com.example.izin.Services.EmployeeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Employee Management")
@Route(value = "")
@CssImport(value = "./styles/GeneratedStyle.css") // CSS dosyasını ekledik

public class MainView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final Grid<Employee> grid = new Grid<>(Employee.class);
    private final Button addButton = new Button("Ekle");
    private final Button updateButton = new Button("Güncelle");
    private final Button deleteButton = new Button("Sil");
    private Employee selectedEmployee;

    // Search Bar için TextField
    private final TextField searchField = new TextField();

    public MainView(EmployeeService employeeService) {
        this.employeeService = employeeService;

        setSizeFull();
        configureSearchBar(); // Search Bar ayarları
        configureGrid();
        configureButtons();

        // Arama çubuğunu ve butonları yatay düzende grupla
        HorizontalLayout actionLayout = new HorizontalLayout(searchField, addButton, updateButton, deleteButton);
        actionLayout.setAlignItems(Alignment.BASELINE); // Elemanları aynı hizaya getir
        actionLayout.setWidthFull(); // Yatay düzenin tüm genişliği kaplamasını sağla
        searchField.setWidthFull(); // Arama çubuğu genişliği

        add(actionLayout, grid); // Arama çubuğunu ve butonları ekle
        updateGrid();
    }

    private void configureSearchBar() {
        searchField.setPlaceholder("Çalışan ara...");
        searchField.addClassName("custom-search-field");
        searchField.addValueChangeListener(event -> filterGrid(event.getValue())); // Değer değiştikçe filtreleme
    }

    private void filterGrid(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            updateGrid(); // Eğer arama boşsa tüm listeyi göster
        } else {
            List<Employee> filteredEmployees = employeeService.findAll().stream()
                    .filter(employee -> employee.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            employee.getLastname().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            (employee.getTckn() != null && employee.getTckn().contains(searchTerm)) ||
                            (employee.getPhoneNumber() != null && employee.getPhoneNumber().contains(searchTerm)))
                    .collect(Collectors.toList());
            grid.setItems(filteredEmployees); // Filtrelenmiş sonuçları göster
        }
    }

    private void configureGrid() {
        grid.removeAllColumns(); // Otomatik olarak eklenen sütunları temizle

        // Sıralama için setSortable(true) kullanıldı
        grid.addColumn(Employee::getName).setHeader("Ad").setSortable(true);
        grid.addColumn(Employee::getLastname).setHeader("Soyad").setSortable(true);
        grid.addColumn(Employee::getTckn).setHeader("Kimlik Numarası");
        grid.addColumn(Employee::getBirthDate).setHeader("Doğum Tarihi").setSortable(true);
        grid.addColumn(Employee::getDateOfEmployment).setHeader("İşe Başlangıç Tarihi").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(employee -> {
            Span statusCircle = new Span();
            statusCircle.getStyle()
                    .set("width", "12px")
                    .set("height", "12px")
                    .set("border-radius", "50%")
                    .set("display", "inline-block")
                    .set("background-color", employee.getStatus() != null && employee.getStatus().equalsIgnoreCase("A")
                            ? "green"
                            : "red");

            return statusCircle;
        })).setHeader("Durum").setAutoWidth(true);

        grid.getColumns().forEach(column -> column.setAutoWidth(true)); // Otomatik genişlik
        grid.asSingleSelect().addValueChangeListener(event -> selectedEmployee = event.getValue());

        grid.addItemDoubleClickListener(event -> {
            Employee employee = event.getItem();
            if (employee != null && employee.getId() > 0) { // ID'nin sıfırdan büyük olduğundan emin olun
                getUI().ifPresent(ui -> ui.navigate("employee-details"));
            } else {
                Notification.show("Geçersiz çalışan ID'si!", 3000, Notification.Position.MIDDLE);
            }
        });
    }


    private void configureButtons() {
        addButton.addClickListener(event -> showAddEmployeeForm());
        updateButton.addClickListener(event -> updateEmployee());
        deleteButton.addClickListener(event -> deleteEmployee());
    }

    private void showAddEmployeeForm() {
        Dialog dialog = new Dialog();
        dialog.addClassName("custom-dialog");
        Span title = new Span("Yeni Çalışan Bilgileri");
        title.addClassName("dialog-title");
        dialog.add(title);

        TextField nameField = new TextField("Ad");
        TextField lastnameField = new TextField("Soyad");
        TextField tcknField = new TextField("Kimlik Numarası");
        TextField phoneField = new TextField("Telefon Numarası");
        DatePicker birthDateField = new DatePicker("Doğum Tarihi");
        DatePicker hireDateField = new DatePicker("İşe Alım Tarihi");

        nameField.setWidth("100%");
        lastnameField.setWidth("100%");
        tcknField.setWidth("100%");
        phoneField.setWidth("100%");
        birthDateField.setWidth("100%");
        hireDateField.setWidth("100%");

        Button saveButton = new Button("Kaydet", event -> {
            try {
                Employee employee = new Employee();
                employee.setName(nameField.getValue());
                employee.setLastname(lastnameField.getValue());
                employee.setTckn(tcknField.getValue());
                employee.setPhoneNumber(phoneField.getValue());
                employee.setBirthDate(birthDateField.getValue() != null ? birthDateField.getValue() : LocalDate.now().minusYears(25));
                employee.setDateOfEmployment(hireDateField.getValue() != null ? hireDateField.getValue() : LocalDate.now());

                employeeService.add(employee);
                Notification.show("Çalışan başarıyla eklendi!");
                updateGrid();
                dialog.close();
            } catch (Exception e) {
                Notification.show("Hata: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("İptal", event -> dialog.close());

        HorizontalLayout dialogButtons = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout dialogLayout = new VerticalLayout(nameField, lastnameField, tcknField, phoneField, birthDateField, hireDateField, dialogButtons);

        dialogLayout.setWidthFull();
        dialog.add(dialogLayout);

        dialog.setWidth("60%");
        dialog.open();
    }

    private void updateEmployee() {
        if (selectedEmployee == null) {
            Notification.show("Güncelleme için bir çalışan seçmelisiniz!", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Seçilen çalışan için bir dialog açma
        Dialog dialog = new Dialog();
        dialog.addClassName("custom-dialog");

        Span title = new Span("Çalışan Bilgilerini Güncelle");
        title.addClassName("dialog-title");
        dialog.add(title);

        // Form alanlarını oluşturma
        TextField nameField = new TextField("Ad", selectedEmployee.getName());
        TextField lastnameField = new TextField("Soyad", selectedEmployee.getLastname());
        TextField tcknField = new TextField("Kimlik Numarası", selectedEmployee.getTckn());
        TextField phoneField = new TextField("Telefon Numarası", selectedEmployee.getPhoneNumber());
        DatePicker birthDateField = new DatePicker("Doğum Tarihi", selectedEmployee.getBirthDate());
        DatePicker hireDateField = new DatePicker("İşe Alım Tarihi", selectedEmployee.getDateOfEmployment());

        nameField.setWidth("100%");
        lastnameField.setWidth("100%");
        tcknField.setWidth("100%");
        phoneField.setWidth("100%");
        birthDateField.setWidth("100%");
        hireDateField.setWidth("100%");

        // Kaydet butonunun işlevselliği
        Button saveButton = new Button("Kaydet", event -> {
            try {
                selectedEmployee.setName(nameField.getValue());
                selectedEmployee.setLastname(lastnameField.getValue());
                selectedEmployee.setTckn(tcknField.getValue());
                selectedEmployee.setPhoneNumber(phoneField.getValue());
                selectedEmployee.setBirthDate(birthDateField.getValue());
                selectedEmployee.setDateOfEmployment(hireDateField.getValue());

                employeeService.update(selectedEmployee); // Veritabanında güncelleme işlemi
                Notification.show("Çalışan bilgileri başarıyla güncellendi!");
                updateGrid(); // Grid'i güncelle
                dialog.close(); // Dialog'u kapat
            } catch (Exception e) {
                Notification.show("Hata: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        // İptal butonunun işlevselliği
        Button cancelButton = new Button("İptal", event -> dialog.close());

        HorizontalLayout dialogButtons = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout dialogLayout = new VerticalLayout(
                nameField, lastnameField, tcknField, phoneField, birthDateField, hireDateField, dialogButtons
        );

        dialogLayout.setWidthFull();
        dialog.add(dialogLayout);

        dialog.setWidth("60%"); // Dialog'un genişliği
        dialog.open();
    }

    private void deleteEmployee() {
        if (selectedEmployee == null) {
            Notification.show("Silmek için bir çalışan seçmelisiniz!", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            employeeService.delete(selectedEmployee);
            Notification.show("Çalışan başarıyla silindi!");
            updateGrid();
        } catch (Exception e) {
            Notification.show("Hata: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void updateGrid() {
        grid.setItems(employeeService.findAll());
    }
}
