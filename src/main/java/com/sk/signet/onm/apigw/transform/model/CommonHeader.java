package com.sk.signet.onm.apigw.transform.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description= "공통헤더")
public class CommonHeader {
	@Schema(description= "어플리케이션 이름", nullable = false)
	private String appName;
	@Schema(description= " 서비스 이름", nullable = false)
	private String svcName;
	@Schema(description= "API Path", nullable = false)
	private String apiPath;
	@Schema(description= "응답코드", nullable = false, example = "COME5501,공백")
	private String responseCode;
	@Schema(description= "사용자아이디", nullable = false)
	private String userId;
	@Schema(description= "사용자언어코드", nullable = true)
	private String langCode;
	@Schema(description= "Company Id", nullable = true, example="회사코드")
	private String cmpnId;
	@Schema(description= "payload 이름", nullable = true)
	private String payloadName;

}
