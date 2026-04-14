package com.tripmoa.global.file;

/**
 * 파일 업로드 디렉토리 상수 정의
 * - 각 도메인별 파일 저장 경로를 구분하기 위한 상수 클래스
 * - 파일 저장 시 directory 파라미터로 사용됨
 * - 사용 예시: fileStorageService.store(file, FileDirectories.EXPENSE);
 * - 실제 저장 경로: uploads/{directory}/파일명
 */
public final class FileDirectories {

    // 각 파일 저장 디렉토리
    public static final String EXPENSE = "expense";
    public static final String VOUCHER = "voucher";
    public static final String BLOG = "blog";

    // 인스턴스 생성 방지
    private FileDirectories() {
    }
}