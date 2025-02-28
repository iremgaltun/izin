package com.example.izin.ui;

import com.example.izin.Model.Employee;
import com.example.izin.Model.Leave;
import com.example.izin.Services.EmployeeService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PageTitle("Employee Management")
@Route(value = "")
public class MainView extends AppLayout {
    private final EmployeeService employeeService;
    private final Grid<Employee> grid = new Grid<>(Employee.class);
    private final Button addButton = new Button("Ekle");
    private final Button updateButton = new Button("Güncelle");
    private final Button deleteButton = new Button("Sil");
    private final TextField searchField = new TextField();
    private Employee selectedEmployee;

    public MainView(EmployeeService employeeService) {
        this.employeeService = employeeService;

        // Navbar ve Sidebar oluşturma
        DrawerToggle toggle = new DrawerToggle();
       createHeader();

        addToDrawer(createSidebarTabs());

        // İçeriği oluştur ve AppLayout'un içerik bölmesine ekle
        setContent(createMainContent());
        updateGrid();
    }
    private void createHeader() {
        H1 title = new H1("Çalışan Yönetimi");
        title.setClassName("header");

        addToNavbar(new DrawerToggle(), title);
    }
    private Tabs createSidebarTabs() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL); // Öğeleri dikey hizala

        Tab employeesTab = new Tab(VaadinIcon.USERS.create(), new RouterLink("Çalışanlar", MainView.class));
        Tab leaveTab = new Tab(VaadinIcon.ARCHIVE.create(), new RouterLink("İzinler", LeaveDetailView.class));

        tabs.add(employeesTab, leaveTab);
        return tabs;
    }


    private VerticalLayout createMainContent() {
        configureSearchBar();
        configureGrid();
        configureButtons();

        HorizontalLayout actionLayout = new HorizontalLayout(searchField, addButton, updateButton, deleteButton);
        actionLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        actionLayout.setWidthFull();
        searchField.setWidthFull();

        VerticalLayout mainLayout = new VerticalLayout(actionLayout, grid);
        mainLayout.setSizeFull();
        return mainLayout;
    }

    private void configureSearchBar() {
        searchField.setPlaceholder("Çalışan ara...");
        searchField.addValueChangeListener(event -> filterGrid(event.getValue()));
    }

    private void filterGrid(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            updateGrid();
        } else {
            List<Employee> filteredEmployees = employeeService.findAll().stream()
                    .filter(employee -> employee.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            employee.getLastname().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            (employee.getTckn() != null && employee.getTckn().contains(searchTerm)))
                    .collect(Collectors.toList());
            grid.setItems(filteredEmployees);
        }
    }

    private void configureGrid() {
        grid.removeAllColumns();

        grid.addColumn(new ComponentRenderer<>(employee -> {
            String imageUrl = "/images/profile-photos/" + employee.getTckn() + ".jpg";
            File photoFile = new File("src/main/resources/static/images/profile-photos/" + employee.getTckn() + ".jpg");

            Avatar avatar = new Avatar(employee.getName());
            avatar.setName("avatar");// İsim göstergesi (isteğe bağlı)

            if (photoFile.exists()) {
                avatar.setImage(imageUrl);
            } else {
                avatar.setAbbreviation(employee.getName().substring(0, 2).toUpperCase()); // Yedek olarak baş harfler
            }



            return avatar;
        })).setHeader("Fotoğraf").setAutoWidth(true);


        grid.addColumn(Employee::getName).setHeader("Ad").setSortable(true);
        grid.addColumn(Employee::getLastname).setHeader("Soyad").setSortable(true);
        grid.addColumn(Employee::getTckn).setHeader("Kimlik Numarası");
        grid.addColumn(Employee::getBirthDate).setHeader("Doğum Tarihi").setSortable(true);
        grid.addColumn(Employee::getDateOfEmployment).setHeader("İşe Başlangıç Tarihi").setSortable(true);
        grid.addColumn(Employee::getPosition).setHeader("Pozisyon");
        grid.addColumn(new ComponentRenderer<>(employee -> {
            Span statusSpan = new Span();
            boolean onLeave = employeeService.isEmployeeOnLeave(employee.getId());

            if (onLeave) {
                statusSpan.setText("İzinde");
                statusSpan.getStyle().set("color", "red");
            } else {
                statusSpan.setText("Çalışıyor");
                statusSpan.getStyle().set("color", "green");
            }
            return statusSpan;
        })).setHeader("Durum");


        grid.getColumns().forEach(column -> column.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> selectedEmployee = event.getValue());

        grid.addItemDoubleClickListener(event -> {
            Employee employee = event.getItem();
            if (employee != null && employee.getId() > 0) {
                getUI().ifPresent(ui -> {
                    ui.navigate(String.format("employee/%d", employee.getId()));
                });
            } else {
                Notification.show("Geçersiz çalışan ID'si!", 3000, Notification.Position.MIDDLE);
            }
        });
    }

    private void configureButtons() {
        addButton.addClickListener(event -> showEmployeeForm(null));
        updateButton.addClickListener(event -> {
            if (selectedEmployee == null) {
                Notification.show("Güncellemek için bir çalışan seçmelisiniz!", 3000, Notification.Position.MIDDLE);
                return;
            }
            showEmployeeForm(selectedEmployee);
        });
        deleteButton.addClickListener(event -> deleteEmployee());
    }
    private void showEmployeeForm(Employee employee) {
        boolean isUpdate = employee != null; // Güncelleme mi yoksa yeni ekleme mi kontrolü
        Employee currentEmployee = isUpdate ? employee : new Employee(); // Mevcut veya yeni çalışan nesnesi

        Dialog dialog = new Dialog();
        dialog.addClassName("custom-dialog");

        Span title = new Span(isUpdate ? "Çalışan Bilgilerini Güncelle" : "Yeni Çalışan Bilgileri");
        title.addClassName("dialog-title");
        dialog.add(title);

        // Form bileşenleri
        TextField tcknField = new TextField("Kimlik Numarası");
        TextField nameField = new TextField("Ad");
        TextField lastnameField = new TextField("Soyad");
        TextField positionField = new TextField("Pozisyon");
        TextField phoneField = new TextField("Telefon Numarası");
        DatePicker birthDateField = new DatePicker("Doğum Tarihi");
        DatePicker hireDateField = new DatePicker("İşe Alım Tarihi");

        tcknField.setWidthFull();
        nameField.setWidthFull();
        lastnameField.setWidthFull();
        positionField.setWidthFull();
        phoneField.setWidthFull();
        birthDateField.setWidthFull();
        hireDateField.setWidthFull();

        // Binder ile formu bağlama
        Binder<Employee> binder = new Binder<>(Employee.class);
        binder.bind(tcknField, Employee::getTckn, Employee::setTckn);
        binder.bind(nameField, Employee::getName, Employee::setName);
        binder.bind(lastnameField, Employee::getLastname, Employee::setLastname);
        binder.bind(positionField, Employee::getPosition, Employee::setPosition);
        binder.bind(phoneField, Employee::getPhoneNumber, Employee::setPhoneNumber);
        binder.bind(birthDateField, Employee::getBirthDate, Employee::setBirthDate);
        binder.bind(hireDateField, Employee::getDateOfEmployment, Employee::setDateOfEmployment);

        // Eğer güncelleme yapılıyorsa, formu mevcut verilerle doldur
        if (isUpdate) {
            binder.readBean(employee);
        }

        // Fotoğraf yükleme bileşeni
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(10 * 1024 * 1024); // 10 MB sınırı

        Span uploadInfo = new Span("Fotoğraf yüklemek için dosya seçin.");
        uploadInfo.setVisible(true);

        AtomicReference<InputStream> photoStream = new AtomicReference<>();
        AtomicReference<String> photoName = new AtomicReference<>();

        upload.addSucceededListener(event -> {
            uploadInfo.setText("Fotoğraf başarıyla yüklendi.");
            photoStream.set(buffer.getInputStream());
            photoName.set(event.getFileName());
        });

        Button saveButton = new Button("Kaydet", event -> {
            try {
                binder.writeBean(currentEmployee); // Formdaki verileri Employee nesnesine yaz

                // Eğer yeni ekleme yapılıyorsa ve TCKN boşsa hata ver
                if (!isUpdate && (tcknField.getValue() == null || tcknField.getValue().isBlank())) {
                    Notification.show("Lütfen geçerli bir Kimlik Numarası girin!", 3000, Notification.Position.MIDDLE);
                    return;
                }

                // TCKN benzersizliği kontrolü
                Optional<Employee> existingEmployee = employeeService.findByTckn(currentEmployee.getTckn());
                if (existingEmployee.isPresent() && !existingEmployee.get().getId().equals(currentEmployee.getId())) {
                    Notification.show("Bu TCKN zaten başka bir çalışan tarafından kullanılıyor.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                // Çalışanı kaydet/güncelle
                if (isUpdate) {
                    employeeService.update(currentEmployee);
                } else {
                    employeeService.add(currentEmployee);
                }

                // Fotoğrafı kaydetme işlemi
                if (photoStream.get() != null) {
                    Path uploadDir = Paths.get("src/main/resources/static/images/profile-photos");
                    if (!Files.exists(uploadDir)) {
                        Files.createDirectories(uploadDir);
                    }

                    // Kullanıcının mevcut fotoğrafı varsa uzantıyı koru
                    String existingPhotoExtension = "";
                    try (Stream<Path> files = Files.list(uploadDir)) {
                        Optional<Path> existingPhoto = files
                                .filter(path -> path.getFileName().toString().startsWith(currentEmployee.getTckn()))
                                .findFirst();
                        if (existingPhoto.isPresent()) {
                            existingPhotoExtension = existingPhoto.get().toString().substring(existingPhoto.get().toString().lastIndexOf("."));
                        }
                    }

                    // Yeni yüklenen fotoğrafın uzantısını al
                    String newPhotoExtension = photoName.get().substring(photoName.get().lastIndexOf("."));

                    // Eğer çalışan için zaten bir fotoğraf varsa, eski uzantıyı koru
                    String fileExtension = existingPhotoExtension.isEmpty() ? newPhotoExtension : existingPhotoExtension;
                    String fileName = currentEmployee.getTckn() + fileExtension;
                    Path filePath = uploadDir.resolve(fileName);

                    try (InputStream inputStream = photoStream.get()) {
                        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                        Notification.show("Fotoğraf başarıyla güncellendi.", 3000, Notification.Position.MIDDLE);
                    }
                }


                Notification.show(isUpdate ? "Çalışan başarıyla güncellendi!" : "Çalışan başarıyla eklendi!");
                updateGrid();
                dialog.close();
            } catch (Exception e) {
                Notification.show("Hata: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("İptal", event -> dialog.close());

        HorizontalLayout dialogButtons = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout dialogLayout = new VerticalLayout(
                tcknField, nameField, lastnameField, positionField, phoneField, birthDateField, hireDateField, upload, uploadInfo, dialogButtons
        );

        dialogLayout.setWidthFull();
        dialog.add(dialogLayout);
        dialog.setWidth("60%");
        dialog.open();
    }

    private void deleteEmployee() {
        if (selectedEmployee == null) {
            Notification.show("Silmek için bir çalışan seçmelisiniz!", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            // Çalışanı veritabanından siliyoruz
            employeeService.delete(selectedEmployee);

            // Fotoğrafın kaydedildiği doğru dizini belirliyoruz
            Path photoPath = Paths.get("C:\\Users\\DELL\\Desktop\\izin\\src\\main\\resources\\static\\images\\profile-photos", selectedEmployee.getTckn() + ".jpg");

            System.out.println("Dosya yolu: " + photoPath.toString()); // Dosya yolunu kontrol et

            if (Files.exists(photoPath)) {
                try {
                    if (Files.isWritable(photoPath)) { // Dosyanın yazılabilir olup olmadığını kontrol et
                        Files.delete(photoPath); // Fotoğraf dosyasını sil
                        Notification.show("Çalışan ve fotoğrafı başarıyla silindi!", 3000, Notification.Position.MIDDLE);
                    } else {
                        Notification.show("Fotoğraf dosyasına yazma izniniz yok.", 3000, Notification.Position.MIDDLE);
                    }
                } catch (IOException e) {
                    Notification.show("Fotoğraf silinemedi: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            } else {
                Notification.show("Çalışan silindi ancak fotoğraf bulunamadı.", 3000, Notification.Position.MIDDLE);
            }

            updateGrid();
        } catch (Exception e) {
            Notification.show("Hata: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void updateGrid() {
        List<Employee> employees = employeeService.findAll();
        System.out.println("updateGrid Çalıştı, Çalışan Sayısı: " + employees.size());

        if (employees.isEmpty()) {
            System.out.println("Veritabanında hiç çalışan yok veya veri çekilemiyor.");
        }

        grid.setItems(employees);
    }


}
