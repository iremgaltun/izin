package com.example.izin.ui;

import com.example.izin.Model.EmpLeaveDTO;
import com.example.izin.Model.Employee;
import com.example.izin.Model.Leave;
import com.example.izin.Services.EmployeeService;
import com.example.izin.Services.LeaveService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@CssImport(value = "./styles/GeneratedStyle.css")
@PageTitle("Employee Details")
@Route(value = "employee/:id")
public class EmpDetail extends AppLayout implements BeforeEnterObserver {

    private final EmployeeService employeeService;
    private final LeaveService leaveService;
    private long employeeId;
    private Grid<Leave> leaveGrid = new Grid<>(Leave.class, false);


    public EmpDetail(EmployeeService employeeService, LeaveService leaveService) {
        this.employeeService = employeeService;
        this.leaveService = leaveService;

        createHeader(); // Başlık oluştur
        setContent(createMainContent()); // Ana içerik alanını ayarla
    }

    private void createHeader() {
        Button homeButton = new Button(
                new Icon(VaadinIcon.ANGLE_LEFT),
                e -> getUI().ifPresent(ui -> ui.navigate(""))
        );

        HorizontalLayout header = new HorizontalLayout(homeButton);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassName("header");

        addToNavbar(header);
    }

    private VerticalLayout createMainContent() {
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeFull();
        contentLayout.addClassName("main-content");
        return contentLayout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("id").orElse(null);
        if (idParam != null) {
            try {
                employeeId = Long.parseLong(idParam);
                Employee employee = employeeService.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

                VerticalLayout contentLayout = (VerticalLayout) getContent();
                contentLayout.removeAll();

                contentLayout.add(createEmployeeInfo(employee)); // Çalışan bilgileri
                contentLayout.add(createLeaveGrid(employee.getId())); // İzinler grid'i
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid employee ID: " + idParam);
            }
        } else {
            throw new RuntimeException("Employee ID is missing in the route.");
        }
    }

    private Div createLeaveGrid(long employeeId) {
        Div leaveGridContainer = new Div();
        leaveGridContainer.addClassName("leave-grid-container");

        H2 title = new H2("İzin Geçmişi");
        title.addClassName("leave-grid-title");

        Button addLeaveButton = new Button("İzin Ekle", VaadinIcon.PLUS.create());
        addLeaveButton.addClassName("add-leave-button");
        addLeaveButton.addClickListener(event -> openLeaveForm(employeeService.findById(employeeId).orElse(null)));

        // Başlık ve butonu içeren layout
        HorizontalLayout headerLayout = new HorizontalLayout(title, addLeaveButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // İzin geçmişini al
        List<Leave> leaves = leaveService.findAll();
        List<Leave> employeeLeaves = leaves.stream()
                .filter(leave -> leave.getEmployee().getId() == employeeId)
                .toList();

        int totalLeaveDays = 40; // Maksimum izin hakkı
        long toplamIzinGunu = employeeLeaves.stream()
                .mapToLong(leave -> ChronoUnit.DAYS.between(leave.getLeaveStart(), leave.getLeaveEnd()))
                .sum();

        double izinOrani = Math.min((double) toplamIzinGunu / totalLeaveDays, 1); // %100 ile sınırlıyoruz

        ProgressBar progressBar = new ProgressBar(0, 1, izinOrani);
        progressBar.setClassName("leave-progress-bar");
        progressBar.setWidth("100%");
        progressBar.setHeight("20px");

        String leaveStatusText = toplamIzinGunu + "/" + totalLeaveDays + " İzin Günü";
        Span leaveStatusLabel = new Span(leaveStatusText);
        leaveStatusLabel.addClassName("leave-status-label");

        // Her durumda header'ı ekle
        leaveGridContainer.add(headerLayout);

        if (employeeLeaves.isEmpty()) {
            leaveGridContainer.add(new Paragraph("Geçmiş İzin Kaydı Bulunamadı."));
        } else {
            Grid<Leave> leaveGrid = new Grid<>(Leave.class, false);

            leaveGrid.addColumn(Leave::getLeaveReason).setHeader("İzin Nedeni").setAutoWidth(true);
            leaveGrid.addColumn(Leave::getLeaveApprover).setHeader("Onaylayan").setAutoWidth(true);
            leaveGrid.addColumn(Leave::getLeaveStart).setHeader("İzin Başlangıç Tarihi").setAutoWidth(true);
            leaveGrid.addColumn(Leave::getLeaveEnd).setHeader("İzin Bitiş Tarihi").setAutoWidth(true);

            // Yeni sütun: Toplam izin gününü hesapla
            leaveGrid.addColumn(leave -> ChronoUnit.DAYS.between(leave.getLeaveStart(), leave.getLeaveEnd()))
                    .setHeader("Toplam Gün")
                    .setAutoWidth(true);
            leaveGrid.addComponentColumn(leave -> {
                Button deleteButton = new Button("Sil ", event -> openDeleteConfirmationDialog(leave));
                deleteButton.addClassName("delete-button");
                return deleteButton;
            }).setHeader("Sil").setWidth("10%");

            leaveGrid.setItems(employeeLeaves);
            leaveGrid.addClassName("leave-grid");
            leaveGrid.setWidthFull();
            leaveGrid.setHeight("300px");

            VerticalLayout leaveGridLayout = new VerticalLayout(
                    leaveStatusLabel, progressBar, leaveGrid
            );
            leaveGridLayout.setSpacing(true);
            leaveGridLayout.setPadding(false);
            leaveGridLayout.setAlignItems(FlexComponent.Alignment.START);

            leaveGridContainer.add(leaveGridLayout);
        }

        return leaveGridContainer;
    }
   private void openDeleteConfirmationDialog(Leave leave) {
        Dialog deleteDialog = new Dialog();
        deleteDialog.setHeaderTitle("İzin Silme");

        // İzin bilgilerini göster
        String confirmationText = "İzin Nedeni: " + leave.getLeaveReason() +
                "\nBaşlangıç Tarihi: " + leave.getLeaveStart() +
                "\nBitiş Tarihi: " + leave.getLeaveEnd() +
                "\nSilmek istediğinizden emin misiniz?";
        Span confirmationSpan = new Span(confirmationText);
        deleteDialog.add(confirmationSpan);

        // Onay butonları
        Button confirmButton = new Button("Evet", event -> {
            try {
                leaveService.delete(leave);  // Silme işlemi
                leaveGrid.setItems(leaveService.findAll());
                leaveGrid.getDataProvider().refreshAll();

                // Grid'i güncellemek ve yeniden render etmek için:
                VerticalLayout contentLayout = (VerticalLayout) getContent();
                contentLayout.removeAll(); // Mevcut içeriği temizle
                contentLayout.add(createEmployeeInfo(employeeService.findById(employeeId).orElse(null))); // Çalışan bilgileri
                contentLayout.add(createLeaveGrid(employeeId)); // İzinler grid'ini yeniden oluştur

                Notification.show("İzin başarıyla silindi.", 3000, Notification.Position.MIDDLE);
                deleteDialog.close();
            } catch (Exception e) {
                Notification.show("Silme hatası: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Hayır", event -> deleteDialog.close());

        // Dialoga butonları ekle
        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        deleteDialog.add(buttonLayout);

        deleteDialog.open();
    }

    private void openLeaveForm(Employee employee) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Yeni İzin Ekle");

        TextField tcNoField = new TextField("TC Kimlik No");
        TextField employeeName = new TextField("Çalışan Adı");
        TextField employeeLastname = new TextField("Çalışan Soyadı");
        TextField leaveReason = new TextField("İzin Nedeni");
        TextField leaveApprover = new TextField("İzin Veren");
        DatePicker leaveStart = new DatePicker("Başlangıç Tarihi");
        leaveStart.setMin(LocalDate.now());

        DatePicker leaveEnd = new DatePicker("Bitiş Tarihi");
        leaveStart.addValueChangeListener(event -> {
            LocalDate startDate = event.getValue();
            if (startDate != null) {
                leaveEnd.setMin(startDate.plusDays(1)); // Başlangıç tarihinden bir gün sonrasını min olarak ayarla
            }
        });


        // Çalışan ID'sini saklamak için dizi (final olmadığı için array kullanıyoruz)
        Long[] employeeIdHolder = new Long[1];

        // Sayfa zaten ilgili çalışana ait, bilgileri doğrudan dolduralım
        if (employee != null) {
            tcNoField.setValue(employee.getTckn());
            employeeName.setValue(employee.getName());
            employeeLastname.setValue(employee.getLastname());
            employeeIdHolder[0] = employee.getId();
        }

        // TC Kimlik No alanını sadece okunur yapalım (manuel girişe izin vermemek için)
        tcNoField.setReadOnly(true);
        employeeName.setReadOnly(true);
        employeeLastname.setReadOnly(true);

        Button saveButton = new Button("Kaydet", event -> {

            EmpLeaveDTO newLeave = new EmpLeaveDTO(
                    employeeIdHolder[0],
                    employeeName.getValue(),
                    employeeLastname.getValue(),
                    leaveReason.getValue(),
                    leaveApprover.getValue(),
                    leaveStart.getValue(),
                    leaveEnd.getValue(),
                    tcNoField.getValue()
            );

            try {
                // Yeni izni kaydet
                leaveService.save(newLeave);

                // Veriyi güncelle
                leaveGrid.setItems(leaveService.findAll());
                leaveGrid.getDataProvider().refreshAll();

                // Grid'i güncellemek ve yeniden render etmek için:
                VerticalLayout contentLayout = (VerticalLayout) getContent();
                contentLayout.removeAll(); // Mevcut içeriği temizle
                contentLayout.add(createEmployeeInfo(employeeService.findById(employeeId).orElse(null))); // Çalışan bilgileri
                contentLayout.add(createLeaveGrid(employeeId)); // İzinler grid'ini yeniden oluştur

                dialog.close();
                Notification.show("İzin başarıyla eklendi.", 3000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Hata: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });




        Button cancelButton = new Button("İptal", event -> dialog.close());

        FormLayout formLayout = new FormLayout(tcNoField, employeeName, employeeLastname, leaveReason, leaveApprover, leaveStart, leaveEnd);
        dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    private Div createEmployeeInfo(Employee employee) {
        Div employeeInfo = new Div();
        employeeInfo.addClassName("employee-info-container");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassName("employee-info-layout");

        Avatar avatar = new Avatar(employee.getName() + " " + employee.getLastname());
        avatar.setClassName("custom-avatar");


        String imageUrl = "/images/profile-photos/" + employee.getTckn() + ".jpg";
        Path imagePath = Paths.get("src/main/resources/static/images/profile-photos/" + employee.getTckn() + ".jpg");

        if (Files.exists(imagePath)) {
            avatar.setImage(imageUrl);
        } else {
            avatar.setAbbreviation(employee.getName().substring(0, 1).toUpperCase() + employee.getLastname().substring(0, 1).toUpperCase());
        }

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        infoLayout.addClassName("employee-info-details");

        H1 name = new H1(employee.getName() + " " + employee.getLastname());
        name.addClassName("name");

        Div infoContainer = new Div();
        infoContainer.addClassName("employee-info-grid");
        infoContainer.add(
                createInfoParagraph("Kimlik Numarası", employee.getTckn()),
                createInfoParagraph("Doğum Tarihi", employee.getBirthDate().toString()),
                createInfoParagraph("Telefon Numarası", employee.getPhoneNumber()),
                createInfoParagraph("Pozisyon", employee.getPosition() != null ? employee.getPosition() : "N/A"),
                createInfoParagraph("İşe Alım Tarihi", employee.getDateOfEmployment().toString())
        );

        // Düzenleme butonu
        Button editButton = new Button("Düzenle", new Icon(VaadinIcon.EDIT));
        editButton.addClickListener(e -> openEditDialog(employee));

        HorizontalLayout buttonLayout = new HorizontalLayout(editButton);
        buttonLayout.setSpacing(true);

        infoLayout.add(name, infoContainer, buttonLayout);
        layout.add(avatar, infoLayout);
        layout.setFlexGrow(1, infoLayout);

        employeeInfo.add(layout);
        return employeeInfo;
    }
    private void openEditDialog(Employee employee) {
        if (employee == null) {
            Notification.show("Güncellenecek çalışan seçilmedi.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.addClassName("custom-dialog");

        Span title = new Span("Çalışan Bilgilerini Güncelle");
        title.addClassName("dialog-title");
        dialog.add(title);

        // Form bileşenleri
        TextField nameField = new TextField("Ad");
        TextField lastnameField = new TextField("Soyad");
        TextField tcknField = new TextField("Kimlik Numarası");
        TextField phoneField = new TextField("Telefon Numarası");
        DatePicker birthDateField = new DatePicker("Doğum Tarihi");
        DatePicker hireDateField = new DatePicker("İşe Alım Tarihi");

        nameField.setWidthFull();
        lastnameField.setWidthFull();
        tcknField.setWidthFull();
        phoneField.setWidthFull();
        birthDateField.setWidthFull();
        hireDateField.setWidthFull();

        // Fotoğraf yükleme bileşeni
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(5 * 1024 * 1024);

        Span uploadInfo = new Span("Fotoğraf yüklemek için dosya seçin.");

        AtomicReference<InputStream> photoStream = new AtomicReference<>();
        AtomicReference<String> photoName = new AtomicReference<>();

        upload.addSucceededListener(event -> {
            uploadInfo.setText("Fotoğraf başarıyla yüklendi.");
            photoStream.set(buffer.getInputStream());
            photoName.set(event.getFileName());
        });

        // Binder ile formu bağlama
        Binder<Employee> binder = new Binder<>(Employee.class);
        binder.bind(nameField, Employee::getName, Employee::setName);
        binder.bind(lastnameField, Employee::getLastname, Employee::setLastname);
        binder.bind(tcknField, Employee::getTckn, Employee::setTckn);
        binder.bind(phoneField, Employee::getPhoneNumber, Employee::setPhoneNumber);
        binder.bind(birthDateField, Employee::getBirthDate, Employee::setBirthDate);
        binder.bind(hireDateField, Employee::getDateOfEmployment, Employee::setDateOfEmployment);

        // Mevcut çalışan bilgilerini forma aktar
        binder.readBean(employee);

        Button saveButton = new Button("Güncelle", event -> {
            try {
                binder.writeBean(employee);

                // TCKN kontrolü
                Optional<Employee> existingEmployee = employeeService.findByTckn(employee.getTckn());
                if (existingEmployee.isPresent() && !existingEmployee.get().getId().equals(employee.getId())) {
                    Notification.show("Bu TCKN zaten başka bir çalışan tarafından kullanılıyor.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                // Güncelleme işlemi
                employeeService.update(employee);
                Notification.show("Çalışan başarıyla güncellendi!", 3000, Notification.Position.MIDDLE);

                // Fotoğraf yükleme işlemi
                if (photoStream.get() != null && photoName.get() != null) {
                    Path uploadDir = Paths.get("src/main/resources/static/images/profile-photos");
                    if (!Files.exists(uploadDir)) {
                        Files.createDirectories(uploadDir);
                    }

                    String fileExtension = photoName.get().substring(photoName.get().lastIndexOf("."));
                    String fileName = employee.getTckn() + fileExtension;
                    Path filePath = uploadDir.resolve(fileName);

                    try (InputStream inputStream = photoStream.get()) {
                        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                        Notification.show("Fotoğraf başarıyla kaydedildi.", 3000, Notification.Position.MIDDLE);
                    }
                }

                dialog.close();
            } catch (IOException e) {
                Notification.show("Dosya kaydetme hatası: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Hata: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("İptal", event -> dialog.close());
        HorizontalLayout dialogButtons = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout dialogLayout = new VerticalLayout(
                nameField, lastnameField, tcknField, phoneField, birthDateField, hireDateField, upload, uploadInfo, dialogButtons
        );

        dialogLayout.setWidthFull();
        dialog.add(dialogLayout);
        dialog.setWidth("60%");
        dialog.open();
    }





    private Paragraph createInfoParagraph(String label, String value) {
        Paragraph paragraph = new Paragraph(label + ": " + value);
        paragraph.addClassName(label.toLowerCase().replace(" ", "-"));
        return paragraph;
    }
}
