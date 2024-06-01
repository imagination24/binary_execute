# Makefile

# 指定你的 Python 文件名
PYTHON_FILE = print.py

# 指定生成的可执行文件名
OUTPUT_NAME = print

# 编译 Python 文件为可执行文件
all: $(OUTPUT_NAME)

$(OUTPUT_NAME): $(PYTHON_FILE)
	cat $(PYTHON_FILE) > $(OUTPUT_NAME)
	chmod a+x $(OUTPUT_NAME)

# 清理生成的文件
clean:
	rm -f $(OUTPUT_NAME)

