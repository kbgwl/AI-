package com.jnysx.aics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jnysx.aics.annotation.RequireLogin;
import com.jnysx.aics.common.Result;
import com.jnysx.aics.entity.KbCategory;
import com.jnysx.aics.entity.KbIndustry;
import com.jnysx.aics.entity.KbItem;
import com.jnysx.aics.entity.UnknownQuestion;
import com.jnysx.aics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库管理 Controller
 * @author 济南空白格网络科技有限公司
 */
@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbIndustryService kbIndustryService;
    private final KbCategoryService kbCategoryService;
    private final KbItemService kbItemService;
    private final UnknownQuestionService unknownQuestionService;

 // ==================== 行业管理 ====================

 @GetMapping("/industries")
 public Result<?> listIndustries() {
  List<KbIndustry> list = kbIndustryService.list(new LambdaQueryWrapper<KbIndustry>().orderByAsc(KbIndustry::getSortOrder));
  return Result.ok(list);
 }

 // ==================== 分类管理 ====================

    @GetMapping("/categories")
    public Result<?> listCategories(@RequestParam(defaultValue = "0") Long parentId) {
        List<KbCategory> list = kbCategoryService.list(
            new LambdaQueryWrapper<KbCategory>()
                .eq(KbCategory::getParentId, parentId)
                .orderByAsc(KbCategory::getSortOrder));
        return Result.ok(list);
    }

    @PostMapping("/category")
    @RequireLogin
    public Result<?> addCategory(@RequestBody KbCategory category) {
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        kbCategoryService.save(category);
        return Result.ok("分类创建成功", category);
    }

    @PutMapping("/category")
    @RequireLogin
    public Result<?> updateCategory(@RequestBody KbCategory category) {
        category.setUpdateTime(LocalDateTime.now());
        kbCategoryService.updateById(category);
        return Result.ok("分类更新成功");
    }

    @DeleteMapping("/category/{id}")
    @RequireLogin
    public Result<?> deleteCategory(@PathVariable Long id) {
        kbCategoryService.removeById(id);
        return Result.ok("分类删除成功");
    }

    // ==================== 知识条目管理 ====================

    @GetMapping("/items")
    public Result<?> listItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<KbItem> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) wrapper.eq(KbItem::getCategoryId, categoryId);
        if (itemType != null) wrapper.eq(KbItem::getItemType, itemType);
        if (keyword != null) wrapper.like(KbItem::getQuestion, keyword)
            .or().like(KbItem::getKeywords, keyword);
        wrapper.orderByDesc(KbItem::getPriority);
        Page<KbItem> result = kbItemService.page(new Page<>(page, size), wrapper);
        return Result.ok(result);
    }

    @GetMapping("/item/{id}")
    public Result<?> getItem(@PathVariable Long id) {
        return Result.ok(kbItemService.getById(id));
    }

    @PostMapping("/item")
    @RequireLogin
    public Result<?> addItem(@RequestBody KbItem item) {
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        item.setHitCount(0);
        item.setLikeCount(0);
        item.setDislikeCount(0);
        item.setVersion(1);
        if (item.getStatus() == null) item.setStatus(1);
        kbItemService.save(item);
        return Result.ok("知识条目创建成功", item);
    }

    @PutMapping("/item")
    @RequireLogin
    public Result<?> updateItem(@RequestBody KbItem item) {
        item.setUpdateTime(LocalDateTime.now());
        item.setVersion(item.getVersion() + 1);
        kbItemService.updateById(item);
        return Result.ok("知识条目更新成功");
    }

    @DeleteMapping("/item/{id}")
    @RequireLogin
    public Result<?> deleteItem(@PathVariable Long id) {
        kbItemService.removeById(id);
        return Result.ok("知识条目删除成功");
    }

    // ==================== 未知问题管理（自学习） ====================

    @GetMapping("/unknown")
    public Result<?> listUnknown(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<UnknownQuestion> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(UnknownQuestion::getStatus, status);
        wrapper.orderByDesc(UnknownQuestion::getCreateTime);
        Page<UnknownQuestion> result = unknownQuestionService.page(new Page<>(page, size), wrapper);
        return Result.ok(result);
    }

    @PostMapping("/unknown/{id}/answer")
    @RequireLogin
    public Result<?> answerUnknown(@PathVariable Long id, @RequestBody UnknownQuestion params) {
        UnknownQuestion uq = unknownQuestionService.getById(id);
        if (uq == null) return Result.fail("问题不存在");
        uq.setSuggestedAnswer(params.getSuggestedAnswer());
        uq.setStatus(1); // 已补充答案
        uq.setHandleBy(params.getHandleBy());
        uq.setHandleTime(LocalDateTime.now());
        unknownQuestionService.updateById(uq);

        // 同步添加到知识库
        if (params.getSuggestedAnswer() != null) {
            KbItem newItem = new KbItem();
            newItem.setQuestion(uq.getQuestion());
            newItem.setAnswer(params.getSuggestedAnswer());
            newItem.setItemType("faq");
            newItem.setStatus(1);
            newItem.setHitCount(0);
            newItem.setLikeCount(0);
            newItem.setDislikeCount(0);
            newItem.setVersion(1);
            kbItemService.save(newItem);
        }
        return Result.ok("已补充答案并同步到知识库");
    }

    @PostMapping("/unknown/{id}/ignore")
    @RequireLogin
    public Result<?> ignoreUnknown(@PathVariable Long id) {
        UnknownQuestion uq = unknownQuestionService.getById(id);
        if (uq != null) {
            uq.setStatus(2);
            uq.setHandleTime(LocalDateTime.now());
            unknownQuestionService.updateById(uq);
        }
        return Result.ok("已忽略");
    }
}
