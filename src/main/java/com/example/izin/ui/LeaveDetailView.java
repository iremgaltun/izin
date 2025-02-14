package com.example.izin.ui;

import com.example.izin.Model.EmpLeaveDTO;
import com.example.izin.Model.Employee;
import com.example.izin.Services.EmployeeService;
import com.example.izin.Services.LeaveService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.notification.Notification;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Route("leave-details")
@PageTitle("İzin Detayları")
public class LeaveDetailView extends AppLayout {
    private final EmployeeService employeeService;
    private final Grid<EmpLeaveDTO> leaveGrid = new Grid<>(EmpLeaveDTO.class);

    private final LeaveService leaveService;

    public LeaveDetailView(EmployeeService employeeService,LeaveService leaveService) {
        this.employeeService = employeeService;
        this.leaveService = leaveService;

        createHeader();  // Navbar (Üst Kısım)
        createDrawer();  // Sidebar (Yan Menü)
        setContent(createContent());  // Ana İçerik
    }

    private void createHeader() {
        H1 title = new H1("İzin Yönetimi");
        title.setClassName("header");

        addToNavbar(new DrawerToggle(), title);
    }

    private void createDrawer() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        Tab employeesTab = new Tab(VaadinIcon.USERS.create(), new RouterLink("Çalışanlar", MainView.class));
        Tab leaveTab = new Tab(VaadinIcon.ARCHIVE.create(), new RouterLink("İzinler", LeaveDetailView.class));

        tabs.add(employeesTab, leaveTab);

        addToDrawer(tabs);
    }


    private VerticalLayout createContent() {
        HorizontalLayout filtersLayout = createDateFilterComponents();
        Button addLeaveButton = new Button("İzin Ekle", VaadinIcon.PLUS.create());
        addLeaveButton.addClickListener(e -> openLeaveForm());

        // Filtre ve butonu aynı satıra almak için
        HorizontalLayout topLayout = new HorizontalLayout(filtersLayout, addLeaveButton);
        topLayout.setWidthFull();
        topLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN); // Elemanları yatayda yay

        configureGrid();
        updateGrid(null, null);

        VerticalLayout contentLayout = new VerticalLayout(topLayout, leaveGrid);
        contentLayout.setSizeFull();
        return contentLayout;
    }


    private HorizontalLayout createDateFilterComponents() {
        ComboBox<String> timeFilterComboBox = new ComboBox<>("Tarih Filtresi");
        timeFilterComboBox.setItems("Son 3 Ay", "Son 6 Ay", "Tümü", "Özel Tarih Aralığı");
        timeFilterComboBox.setValue("Tümü");

        DatePicker startDatePicker = new DatePicker("Başlangıç Tarihi");
        DatePicker endDatePicker = new DatePicker("Bitiş Tarihi");
        startDatePicker.setVisible(false);
        endDatePicker.setVisible(false);

        timeFilterComboBox.addValueChangeListener(event -> {
            String selectedFilter = event.getValue();
            LocalDate filterDate = null;

            switch (selectedFilter) {
                case "Son 3 Ay" -> filterDate = LocalDate.now().minusMonths(3);
                case "Son 6 Ay" -> filterDate = LocalDate.now().minusMonths(6);
                case "Özel Tarih Aralığı" -> {
                    startDatePicker.setVisible(true);
                    endDatePicker.setVisible(true);
                    return;
                }
                case "Tümü" -> {
                    updateGrid(null, null); // Tüm izinleri listele
                    startDatePicker.setVisible(false);
                    endDatePicker.setVisible(false);
                    return;
                }
            }

            startDatePicker.setVisible(false);
            endDatePicker.setVisible(false);
            updateGrid(filterDate, LocalDate.now());
        });

        startDatePicker.addValueChangeListener(event -> updateGrid(startDatePicker.getValue(), endDatePicker.getValue()));
        endDatePicker.addValueChangeListener(event -> updateGrid(startDatePicker.getValue(), endDatePicker.getValue()));

        HorizontalLayout layout = new HorizontalLayout(timeFilterComboBox, startDatePicker, endDatePicker);
        layout.setWidthFull();
        return layout;
    }
    private void openLeaveForm() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Yeni İzin Ekle");

        TextField tcNoField = new TextField("TC Kimlik No");
        TextField employeeName = new TextField("Çalışan Adı");
        TextField employeeLastname = new TextField("Çalışan Soyadı");
        TextField leaveReason = new TextField("İzin Nedeni");
        TextField leaveApprover = new TextField("İzin Veren");
        DatePicker leaveStart = new DatePicker("Başlangıç Tarihi");
        DatePicker leaveEnd = new DatePicker("Bitiş Tarihi");

        // Çalışan ID'sini saklamak için dizi (final olmadığı için array kullanıyoruz)
        Long[] employeeIdHolder = new Long[1];

        // TC Kimlik No girildiğinde çalışan bilgilerini getir
        tcNoField.addValueChangeListener(event -> {
            String tcNo = event.getValue();
            Optional<Employee> employee = employeeService.findByTckn(tcNo);
            if (employee.isPresent()) {
                employeeName.setValue(employee.get().getName());
                employeeLastname.setValue(employee.get().getLastname());
                employeeIdHolder[0] = employee.get().getId(); // Çalışan ID'sini sakla
            } else {
                Notification.show("Çalışan bulunamadı!", 3000, Notification.Position.MIDDLE);
                employeeName.clear();
                employeeLastname.clear();
                employeeIdHolder[0] = null; // ID'yi sıfırla
            }
        });

        // Kaydet butonu işlemi
        Button saveButton = new Button("Kaydet", event -> {
            if (employeeIdHolder[0] == null) {
                Notification.show("Geçerli bir çalışan seçmelisiniz!", 3000, Notification.Position.MIDDLE);
                return;
            }

            EmpLeaveDTO newLeave = new EmpLeaveDTO(
                    employeeIdHolder[0],  // Çalışanın ID'si
                    employeeName.getValue(),
                    employeeLastname.getValue(),
                    leaveReason.getValue(),
                    leaveApprover.getValue(),
                    leaveStart.getValue(),
                    leaveEnd.getValue(),
                    tcNoField.getValue() // DTO'da 'tckn' olarak tanımlandığı için
            );

            try {
                leaveService.save(newLeave);
                updateGrid(null, null);
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




    private void configureGrid() {
        leaveGrid.removeAllColumns();
        leaveGrid.addColumn(leave -> leave.getEmployeeName() + " " + leave.getEmployeeLastname())
                .setHeader("Çalışan Adı Soyadı");
        leaveGrid.addColumn(EmpLeaveDTO::getLeaveReason).setHeader("İzin Nedeni");
        leaveGrid.addColumn(EmpLeaveDTO::getLeaveApprover).setHeader("İzin Veren");
        leaveGrid.addColumn(EmpLeaveDTO::getLeaveStart).setHeader("Başlangıç Tarihi").setSortable(true);
        leaveGrid.addColumn(EmpLeaveDTO::getLeaveEnd).setHeader("Bitiş Tarihi").setSortable(true);

        leaveGrid.getColumns().forEach(column -> column.setAutoWidth(true));

        leaveGrid.addItemDoubleClickListener(event -> {
            EmpLeaveDTO selectedLeave = event.getItem();
            if (selectedLeave != null && selectedLeave.getId() != null) {
                getUI().ifPresent(ui -> {
                    ui.navigate(String.format("employee/%d", selectedLeave.getId()));
                });
            } else {
                Notification.show("Geçersiz çalışan ID'si!", 3000, Notification.Position.MIDDLE);
            }
        });
    }

    private void updateGrid(LocalDate filterDate, LocalDate specificDate) {
        List<EmpLeaveDTO> leaveDetails = employeeService.getAllEmployeeLeaveDetails();

        if (filterDate != null) {
            leaveDetails = leaveDetails.stream()
                    .filter(leave -> !leave.getLeaveStart().isBefore(filterDate))
                    .toList();
        }

        if (specificDate != null) {
            leaveDetails = leaveDetails.stream()
                    .filter(leave ->
                            (leave.getLeaveStart().isEqual(specificDate) || leave.getLeaveStart().isBefore(specificDate)) &&
                                    (leave.getLeaveEnd().isEqual(specificDate) || leave.getLeaveEnd().isAfter(specificDate))
                    ).toList();
        }

        leaveGrid.setItems(leaveDetails);

        if (leaveDetails.isEmpty()) {
            Notification.show("Belirtilen filtreye göre kayıt bulunamadı.", 3000, Notification.Position.MIDDLE);
        }
    }
}
