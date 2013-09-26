# Copyright (C) 2013 Sebastian Pipping <sebastian@pipping.org>
# Licensed under GPL v3 or later

JAVA ?= java
JAVAC ?= javac
MKDIR_P ?= mkdir -p
LN_S ?= ln -s
JCFLAGS =


# Build class path
noop =
space = $(whatever) $(whatever)
BASE_DIR = $(shell pwd)
EXTERNAL_DIR = $(BASE_DIR)/lib
JAR_FILES = $(wildcard $(EXTERNAL_DIR)/*.jar)

SOURCE_BASE = $(BASE_DIR)/src
BINARY_BASE = $(BASE_DIR)/bin

CLASS_PATH_BUILD = $(subst $(space),:,$(JAR_FILES))
CLASS_PATH_RUN = $(BINARY_BASE):$(subst $(space),:,$(JAR_FILES))

JAVA_FILES = $(shell find $(SOURCE_BASE) -type f -name '*.java')
CLASS_FILES = $(patsubst $(SOURCE_BASE)/%,$(BINARY_BASE)/%,$(patsubst %.java,%.class,$(JAVA_FILES)))

all: build

$(BINARY_BASE):
	$(MKDIR_P) $@

define TEMPLATE
$(1): $(2) | $(BINARY_BASE)
	@echo "  JAVAC   $$@"
	@$$(JAVAC) $$(JCFLAGS) -classpath "$$(CLASS_PATH_BUILD)" -sourcepath "$$(SOURCE_BASE)" -d "$$(BINARY_BASE)" "$$<"
endef

# Make a rule for each .class/.java pair
$(foreach class_file,\
  $(CLASS_FILES),\
  $(eval \
    $(call TEMPLATE,\
      $(class_file),\
      $(patsubst %.class,\
        %.java,\
        $(patsubst $(BINARY_BASE)/%,\
          $(SOURCE_BASE)/%,\
          $(class_file)\
        )\
      )\
     )\
  )\
)

$(BINARY_BASE)/edu/drexel/psal/resources:
	$(LN_S) ../../../../$(SOURCE_BASE)/edu/drexel/psal/resources $(BINARY_BASE)/edu/drexel/psal/resources

.PHONY: build
build: $(CLASS_FILES) $(BINARY_BASE)/edu/drexel/psal/resources

.PHONY: clean
clean:
	find "$(BINARY_BASE)" -type f -name '*.class' -delete

.PHONY: run
run: build
	( $(JAVA) -Dfile.encoding=UTF-8 -classpath "$(CLASS_PATH_RUN):." edu.drexel.psal.anonymouth.gooie.ThePresident )
