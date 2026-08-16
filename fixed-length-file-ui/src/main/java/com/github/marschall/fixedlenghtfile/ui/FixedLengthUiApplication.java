package com.github.marschall.fixedlenghtfile.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.SignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.SegmentFieldDefinition;
import com.github.marschall.fixedlenghtfile.FileDefinition;
import com.github.marschall.fixedlenghtfile.FixedLengthFileParser;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;
import com.github.marschall.fixedlenghtfile.SegmentIndicator;
import com.github.marschall.fixedlenghtfile.StatefulFixedLengthFile;
import com.github.marschall.fixedlenghtfile.StatefulFixedLengthFile.LineLocator;
import com.github.marschall.fixedlenghtfile.configuration.parser.InterfaceDefinition267;
import com.github.marschall.fixedlenghtfile.ui.ColumnModel.ValueAccessor;

public class FixedLengthUiApplication {

  private final FileDefinition definition;
  private final List<ColumnModel> columnModels;
  private FixedLengthTableModel dataModel;

  FixedLengthUiApplication(FileDefinition definition) {
    this.definition = definition;
    this.columnModels = buildColumnModelList(definition, "KT");
  }

  JPanel createPanel() {
    JPanel panel = new JPanel(new GridLayout(1,0));

    JTable table = new JTable();
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    table.setFillsViewportHeight(true);
    this.dataModel = new FixedLengthTableModel(this.columnModels);
    table.setModel(this.dataModel);

    JScrollPane scrollPane = new JScrollPane(table);
    panel.add(scrollPane);

    return panel;
  }
  
  void loadFile(Path path) {
    Thread loader = new Thread(() -> {
      Arena arena = Arena.ofAuto();
      try {
        StatefulFixedLengthFile file = FixedLengthFileParser.parseFile(this.definition, path, arena);
        List<LineLocator> locators = file.preparseFile("KT");
        SwingUtilities.invokeLater(() -> {
          dataModel.loadFile(file, locators);
        });
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
      
    }, "data-loader");
    loader.start();
  }

  static List<ColumnModel> buildColumnModelList(FileDefinition fileDefinition, String recordType) {
    SegmentedRecordDefinition recordDefinition = (SegmentedRecordDefinition) fileDefinition.getRecordDefinition(recordType);
    List<ColumnModel> models = new ArrayList<>();
    for (OffsetFieldDefinition fieldDefinition : recordDefinition.getFixedRecords()) {
      String fieldName = fieldDefinition.getName();
      Class<?> valueType = getValueType(fieldDefinition);
      ValueAccessor accessor = getAccessor(fieldDefinition);
      models.add(new ColumnModel(fieldName, valueType, accessor));
    }
    for (SegmentDefinition segmentDefinition : recordDefinition.getSegmentDefinitions()) {
      StringFieldDefinition segmentIndicatorField = segmentDefinition.getSegmentIndicatorField();
      for (SegmentFieldDefinition<?> fieldDefinition : segmentDefinition.getFields()) {
        String fieldName = fieldDefinition.getName();
        Class<?> valueType = getValueType(fieldDefinition.getDelegate());
        ValueAccessor accessor = getAccessor(segmentIndicatorField, fieldDefinition);
        models.add(new ColumnModel(fieldName, valueType, accessor));
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

  void createAndShowGUI() {
    JFrame frame = new JFrame("FixedLength File");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel newContentPane = createPanel();
    newContentPane.setOpaque(true);
    frame.setContentPane(newContentPane);

    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    FileDefinition definition = InterfaceDefinition267.definition();
    SwingUtilities.invokeLater(() -> {
      var application = new FixedLengthUiApplication(definition);
      application.createAndShowGUI();
//      application.loadFile(Path.of(""));
    });
  }

}
