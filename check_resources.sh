#!/bin/bash

echo "=== 检查Android资源 ==="
echo ""

# 检查mipmap目录
echo "✓ mipmap目录："
for dir in app/src/main/res/mipmap-*; do
  if [ -d "$dir" ]; then
    count=$(ls -1 "$dir" | wc -l)
    echo "  $(basename $dir): $count 个文件"
  fi
done

echo ""
echo "✓ drawable目录："
if [ -d "app/src/main/res/drawable" ]; then
  count=$(ls -1 app/src/main/res/drawable | wc -l)
  echo "  drawable: $count 个文件"
fi

echo ""
echo "✓ values目录："
if [ -d "app/src/main/res/values" ]; then
  count=$(ls -1 app/src/main/res/values | wc -l)
  echo "  values: $count 个文件"
fi

echo ""
echo "✓ 检查strings.xml："
if grep -q "app_name" app/src/main/res/values/strings.xml 2>/dev/null; then
  echo "  ✓ app_name 已定义"
else
  echo "  ✗ app_name 未定义"
fi

echo ""
echo "✓ 检查styles.xml："
if grep -q "Theme.YanbaoCamera" app/src/main/res/values/styles.xml 2>/dev/null; then
  echo "  ✓ Theme.YanbaoCamera 已定义"
else
  echo "  ✗ Theme.YanbaoCamera 未定义"
fi

