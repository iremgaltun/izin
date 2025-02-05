package com.example.izin.ui;

import com.example.izin.Model.Employee;
import com.example.izin.Model.Leave;
import com.example.izin.Services.EmployeeService;
import com.example.izin.Services.LeaveService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@CssImport(value = "./styles/GeneratedStyle.css")
@PageTitle("Employee Details")
@Route(value = "employee/:id")
public class EmpDetail extends VerticalLayout implements BeforeEnterObserver {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private long employeeId;
    private final Button editButton = new Button("Düzenle");

    public EmpDetail(EmployeeService employeeService, LeaveService leaveService) {
        this.employeeService = employeeService;
        this.leaveService = leaveService;
        addClassName("employee-details-view");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("id").orElse(null);
        if (idParam != null) {
            try {
                employeeId = Long.parseLong(idParam);
                Employee employee = employeeService.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

                add(createEmployeeInfo(employee)); // Çalışan bilgileri
                add(createLeaveGrid(employee.getId())); // İzinler grid'i
               // Düzenle butonunu ekle
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid employee ID: " + idParam);
            }
        } else {
            throw new RuntimeException("Employee ID is missing in the route.");
        }
    }


    private Dialog createEditForm(Employee employee) {
        Dialog dialog = new Dialog();

        // Form alanları
        TextField nameField = new TextField("Ad");
        nameField.setValue(employee.getName() != null ? employee.getName() : "");

        TextField lastnameField = new TextField("Soyad");
        lastnameField.setValue(employee.getLastname() != null ? employee.getLastname() : "");

        TextField tcknField = new TextField("Kimlik Numarası");
        tcknField.setValue(employee.getTckn() != null ? employee.getTckn() : "");
        tcknField.setEnabled(false); // Kimlik numarası düzenlenemez

        TextField phoneField = new TextField("Telefon");
        phoneField.setValue(employee.getPhoneNumber() != null ? employee.getPhoneNumber() : "");

        DatePicker birthDateField = new DatePicker("Doğum Tarihi");
        birthDateField.setValue(employee.getBirthDate());

        DatePicker employmentDateField = new DatePicker("İşe Alım Tarihi");
        employmentDateField.setValue(employee.getDateOfEmployment());

        birthDateField.addClassName("custom-date-picker");
        employmentDateField.addClassName("custom-date-picker");

        // Form alanlarının genişlik ayarı
        nameField.setWidth("100%");
        lastnameField.setWidth("100%");
        tcknField.setWidth("100%");
        phoneField.setWidth("100%");
        birthDateField.setWidth("100%");
        employmentDateField.setWidth("100%");

        // Kaydetme ve İptal butonları
        Button saveButton = new Button("Kaydet", e -> {
            // Formdan değerleri al ve güncelle
            employee.setName(nameField.getValue());
            employee.setLastname(lastnameField.getValue());
            employee.setPhoneNumber(phoneField.getValue());
            employee.setBirthDate(birthDateField.getValue());
            employee.setDateOfEmployment(employmentDateField.getValue());

            employeeService.update(employee); // Veritabanını güncelle
            dialog.close();

            // Sayfayı yenile
            getUI().ifPresent(ui -> ui.navigate("employee/" + employee.getId()));
        });

        Button cancelButton = new Button("İptal", e -> dialog.close());

        // Buton düzeni ve form düzeni
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout formLayout = new VerticalLayout(
                tcknField, nameField, lastnameField, phoneField,
                birthDateField, employmentDateField, buttonLayout
        );

        dialog.add(formLayout);

        return dialog;
    }

    private Div createEmployeeInfo(Employee employee) {
        Div employeeInfo = new Div();
        employeeInfo.addClassName("employee-info-container");

        // Fotoğraf ve bilgileri yatay düzen içinde tutma
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassName("employee-info-layout");
        Avatar avatar = new Avatar();
        avatar.setName(employee.getName() + " " + employee.getLastname());
        avatar.setName("custom-avatar");
        avatar.getStyle()
                .set("width", "150px")
                .set("height", "150px")
                .set("border-radius", "50%")
                .set("object-fit", "cover")
                .set("margin-left", "8px")
                .set("margin-right", "16px");

        // Fotoğraf yolu oluşturma
        String imageUrl = "/images/profile-photos/" + employee.getTckn() + ".jpg";
        Path imagePath = Paths.get("src/main/resources/static/images/profile-photos/" + employee.getTckn() + ".jpg");


        if (Files.exists(imagePath)) {
            avatar.setImage(imageUrl);
        } else {
            avatar.setAbbreviation(employee.getName().substring(0, 1).toUpperCase() + employee.getLastname().substring(0, 1).toUpperCase());
        }

        // Çalışan bilgileri
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        infoLayout.addClassName("employee-info-details");

        H1 name = new H1(employee.getName() + " " + employee.getLastname());
        name.addClassName("name");


        // Ad ve bilgi grubunu ekleme
        Div infoContainer = new Div();
        infoContainer.addClassName("employee-info-grid");
        infoContainer.add(
                createInfoParagraph("Kimlik Numarası", employee.getTckn()),
                createInfoParagraph("Doğum Tarihi", employee.getBirthDate().toString()),
                createInfoParagraph("Telefon Numarası", employee.getPhoneNumber()),
                createInfoParagraph("Pozisyon", employee.getPosition() != null ? employee.getPosition(): "N/A"),
                createInfoParagraph("İşe Alım Tarihi", employee.getDateOfEmployment().toString())
        );

        infoLayout.add(name, infoContainer);
        layout.add(avatar, infoLayout);
        layout.setFlexGrow(1, infoLayout);

        employeeInfo.add(layout);
        return employeeInfo;
    }
    private Paragraph createInfoParagraph(String label, String value) {
        Paragraph paragraph = new Paragraph(label + ": " + value);
        paragraph.addClassName(label.toLowerCase().replace(" ", "-"));
        return paragraph;
    }

    private Div createLeaveGrid(long employeeId) {
        Div leaveGridContainer = new Div();
        leaveGridContainer.addClassName("leave-grid-container");

        H2 title = new H2("İzin Geçmişi");
        title.addClassName("leave-grid-title");

        List<Leave> leaves = leaveService.findAll();
        List<Leave> employeeLeaves = leaves.stream()
                .filter(leave -> leave.getEmployee().getId() == employeeId)
                .toList();

        if (employeeLeaves.isEmpty()) {
            leaveGridContainer.add(new Paragraph("Geçmiş İzin Kaydı Bulunamadı."));
        } else {
            Grid<Leave> leaveGrid = new Grid<>(Leave.class, false);

            leaveGrid.addColumn(Leave::getLeaveReason).setHeader("İzin Nedeni").setAutoWidth(true);
            leaveGrid.addColumn(Leave::getLeaveApprover).setHeader("Onaylayan").setAutoWidth(true);
            leaveGrid.addColumn(Leave::getLeaveStart).setHeader("İzin Başlangıç Tarihi").setAutoWidth(true);
            leaveGrid.addColumn(Leave::getLeaveEnd).setHeader("İzin Bitiş Tarihi").setAutoWidth(true);

            leaveGrid.setItems(employeeLeaves);

            leaveGrid.addClassName("leave-grid");
            leaveGrid.setWidthFull();
            leaveGrid.setHeight("300px");

            leaveGridContainer.add(title, leaveGrid);
        }

        return leaveGridContainer;
    }
}
