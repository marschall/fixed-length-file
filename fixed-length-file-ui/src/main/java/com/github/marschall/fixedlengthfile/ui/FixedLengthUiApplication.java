package com.github.marschall.fixedlengthfile.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;

import com.github.marschall.fixedlengthfile.AbstractFixedLengthFileParser;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.SignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.SegmentFieldDefinition;
import com.github.marschall.fixedlengthfile.FileDefinition;
import com.github.marschall.fixedlengthfile.FileDefinitionRepository;
import com.github.marschall.fixedlengthfile.RepositoryFixedLengthFileParser;
import com.github.marschall.fixedlengthfile.RecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;
import com.github.marschall.fixedlengthfile.SegmentIndicator;
import com.github.marschall.fixedlengthfile.StatefulFixedLengthFile;
import com.github.marschall.fixedlengthfile.StatefulFixedLengthFile.LineLocator;
import com.github.marschall.fixedlengthfile.configuration.parser.InterfaceDefinition267;
import com.github.marschall.fixedlengthfile.configuration.parser.InterfaceDefinition300;
import com.github.marschall.fixedlengthfile.ui.ColumnModel.ValueAccessor;

public class FixedLengthUiApplication {

  private final AbstractFixedLengthFileParser parser;
  private final List<Path> openFiles;
  private JTabbedPane tabbedPane;
  private final ExecutorService backgroundLoader;

  FixedLengthUiApplication(FileDefinitionRepository repository) {
    this.parser = new RepositoryFixedLengthFileParser(repository);
    this.openFiles = Collections.synchronizedList(new ArrayList<>());
    this.backgroundLoader = Executors.newSingleThreadExecutor(runnable -> {
      var thread = new Thread(runnable, "background-loader");
      thread.setDaemon(true);
      return thread;
    });
  }

  JPanel createContentPane() {
    var panel = new JPanel(new GridLayout(1, 1));

    this.tabbedPane = new JTabbedPane();
    this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    panel.add(this.tabbedPane);

    return panel;
  }

  JPanel createTablePanel(FixedLengthTableModel dataModel) {
    var panel = new JPanel(new GridLayout(1, 0));

    var table = new JTable();
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    table.setFillsViewportHeight(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setModel(dataModel);

    setColumnWidths(table, dataModel);

    var scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    panel.add(scrollPane);

    return panel;
  }

  private void setColumnWidths(JTable table, FixedLengthTableModel dataModel) {
    List<ColumnModel> columnModels = dataModel.getColumnModelList();
    var tableFontMetrics = table.getFontMetrics(table.getFont());
    for (int i = 0; i < columnModels.size(); i++) {
      ColumnModel columnModel = columnModels.get(i);
      TableColumn column = table.getColumnModel().getColumn(i);
      String reference  = "X".repeat(columnModel.getColumnWidth());
      int stringWidth = tableFontMetrics.stringWidth(reference);
      column.setPreferredWidth(stringWidth + 10);
    }
  }

  private void openFile(Path path) {
    synchronized (this.openFiles) {
      // search open files first
      for (int i = 0; i < this.openFiles.size(); i++) {
        Path openFile = this.openFiles.get(i);
        try {
          if (Files.isSameFile(openFile, path)) {
            this.selectTab(i);
            return;
          }
        } catch (IOException e) {
          e.printStackTrace(System.err);
        }
      }
    }
    this.loadFileInBackground(path);
  }

  private void selectTab(int index) {
    SwingUtilities.invokeLater(() -> this.tabbedPane.setSelectedIndex(index));
  }

  private void loadFileInBackground(Path path) {
    this.backgroundLoader.submit(() -> {
      Arena arena = Arena.ofAuto();
      try {
        StatefulFixedLengthFile file = FixedLengthUiApplication.this.parser.parseFile(path, arena);
        FileDefinition fileDefinition = file.getFileDefinition();
        RecordDefinition mainRecordType = fileDefinition.getRecordDefinitions().get(1);
        List<LineLocator> locators = file.preparseFile(mainRecordType.getType());
        List<ColumnModel> columnModels = buildColumnModelList(fileDefinition, mainRecordType.getType());
        FixedLengthTableModel tableModel = new FixedLengthTableModel(columnModels);
        tableModel.loadFile(file, locators);
        SwingUtilities.invokeLater(() -> addTab(path, tableModel));
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    });
  }
  
  void addTab(Path file, FixedLengthTableModel tableModel) {
    JPanel tablePanel = createTablePanel(tableModel);
    this.tabbedPane.addTab(file.getFileName().toString(), tablePanel);
    this.openFiles.add(file);
  }

  static List<ColumnModel> buildColumnModelList(FileDefinition fileDefinition, String recordType) {
    SegmentedRecordDefinition recordDefinition = (SegmentedRecordDefinition) fileDefinition.getRecordDefinition(recordType);
    List<ColumnModel> models = new ArrayList<>();
    for (OffsetFieldDefinition fieldDefinition : recordDefinition.getFixedFields()) {
      Class<?> valueType = getValueType(fieldDefinition);
      ValueAccessor accessor = getAccessor(fieldDefinition);
      models.add(new ColumnModel(valueType, accessor, fieldDefinition));
    }
    for (SegmentDefinition segmentDefinition : recordDefinition.getSegmentDefinitions()) {
      StringFieldDefinition segmentIndicatorField = segmentDefinition.getSegmentIndicatorField();
      for (SegmentFieldDefinition<?> fieldDefinition : segmentDefinition.getFields()) {
        OffsetFieldDefinition delegateFieldDefinition = fieldDefinition.getDelegate();
        Class<?> valueType = getValueType(delegateFieldDefinition);
        ValueAccessor accessor = getAccessor(segmentIndicatorField, fieldDefinition);
        models.add(new ColumnModel(valueType, accessor, delegateFieldDefinition));
      }
    }
    return models;
  }

  private static Class<?> getValueType(OffsetFieldDefinition fieldDefinition) {
    return switch (fieldDefinition) {
      case SignedFieldDefinition _ -> {
        yield fieldDefinition.getLength() <= 8 ? Integer.class : Long.class;
      }
      case UnsignedFieldDefinition _ -> {
        yield fieldDefinition.getLength() <= 9 ? Integer.class : Long.class;
      }
      case StringFieldDefinition _ -> {
        yield String.class;
      }
    };
  }

  private static ValueAccessor getAccessor(OffsetFieldDefinition fieldDefinition) {
    return switch (fieldDefinition) {
      case SignedFieldDefinition _ -> {
        throw new IllegalStateException("singed not yet implemented");
      }
      case UnsignedFieldDefinition field -> {
        if (fieldDefinition.getLength() <= 9) {
          yield line -> line.readUnsignedInt(field);
        } else {
          yield line -> line.readUnsignedLong(field);
        }
      }
      case StringFieldDefinition field -> {
        yield line -> line.readTrimmedString(field);
      }
    };
  }

  private static ValueAccessor getAccessor(StringFieldDefinition segmentIndicatorField, SegmentFieldDefinition<?> segmentFieldDefinition) {
    OffsetFieldDefinition delegateFieldDefinition = segmentFieldDefinition.getDelegate();
    return switch (delegateFieldDefinition) {
      case SignedFieldDefinition _ -> {
        throw new IllegalStateException("singed not yet implemented");
      }
      case UnsignedFieldDefinition _ -> {
        SegmentFieldDefinition<UnsignedFieldDefinition> definition = (SegmentFieldDefinition<UnsignedFieldDefinition>) segmentFieldDefinition;
        if (delegateFieldDefinition.getLength() <= 9) {
          yield line -> {
            var segmementIndicator = line.readSegmentIndicator(segmentIndicatorField);
            if (segmementIndicator == SegmentIndicator.PRESENT) {
              return line.readUnsignedInt(definition);
            } else {
              return null;
            }
          };
        } else {
          yield line -> {
            var segmementIndicator = line.readSegmentIndicator(segmentIndicatorField);
            if (segmementIndicator == SegmentIndicator.PRESENT) {
              return line.readUnsignedLong(definition);
            } else {
              return null;
            }
          };
        }
      }
      case StringFieldDefinition _ -> {
        SegmentFieldDefinition<StringFieldDefinition> definition = (SegmentFieldDefinition<StringFieldDefinition>) segmentFieldDefinition;
        yield line -> {
          var segmementIndicator = line.readSegmentIndicator(segmentIndicatorField);
          if (segmementIndicator == SegmentIndicator.PRESENT) {
            return line.readTrimmedString(definition);
          } else {
            return null;
          }
        };
      }
    };
  }

  private JMenuBar createMenuBar(JFrame frame) {
    var menuBar = new JMenuBar();

    var fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    var openItem = new JMenuItem("Open File...", KeyEvent.VK_O);
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    openItem.addActionListener(event -> this.openFileAction(event, frame));
    fileMenu.add(openItem);
    fileMenu.addSeparator();
    var exit = new JMenuItem("Exit");
    exit.addActionListener(_ -> System.exit(0));
    fileMenu.add(exit);

    menuBar.add(fileMenu);

    return menuBar;
  }

  void openFileAction(ActionEvent event, JFrame parent) {
    var fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(true);
    fileChooser.setAcceptAllFileFilterUsed(true);
    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("KT File", "kt"));

    int result = fileChooser.showOpenDialog(parent);
    if (result == JFileChooser.APPROVE_OPTION) {
      File[] selectedFiles = fileChooser.getSelectedFiles();
      for (File selectedFile : selectedFiles) {
        openFile(selectedFile.toPath());
      }
    }
  }

  void createAndShowGUI() {
    JFrame frame = new JFrame("FixedLength File Viewer");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel contentPane = createContentPane();
    contentPane.setOpaque(true);
    contentPane.setPreferredSize(new Dimension(500, 280));
    frame.setContentPane(contentPane);

    frame.setJMenuBar(createMenuBar(frame));

    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    FileDefinitionRepository repository = FileDefinitionRepository.builder()
        .addMainFileDefinition(InterfaceDefinition267.definition())
        .addVariantFileDefinition(InterfaceDefinition300.definition())
        .build();
    SwingUtilities.invokeLater(() -> {
      var application = new FixedLengthUiApplication(repository);
      application.createAndShowGUI();
      for (String arg : args) {
        Path toOpen = Paths.get(arg);
        if (Files.exists(toOpen) && Files.isReadable(toOpen)) {
          application.openFile(toOpen);
        }
      }
    });
  }

}
