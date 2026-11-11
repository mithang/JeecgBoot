/*
 *
 *  *  Copyright (c) 2019-2020, 冷冷 (wangiegie@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.jeecg.common.constant;

/**
 * @author scott
 * @date 2019 năm 05 tháng 18 ngày
 * Tên dịch vụ
 */
public interface ServiceNameConstants {

	/**
	 * Tên microservice: Mô-đun quản lý hệ thống
	 */
	String SERVICE_SYSTEM = "jeecg-system";
	/**
	 * Tên microservice: Mô-đun demo
	 */
	String SERVICE_DEMO = "jeecg-demo";
	/**
	 * Tên microservice: Mô-đun joa
	 */
	String SERVICE_JOA = "jeecg-joa";
	
//	/**
//	 * Tên microservice: Mô-đun online trực tuyến
//	 */
//	String SERVICE_ONLINE = "jeecg-online";
//	/**
//	 * Tên microservice: Mô-đun OA
//	 */
//	String SERVICE_EOA = "jeecg-eoa";
//	/**
//	 * Tên microservice: Mô-đun thiết kế biểu mẫu
//	 */
//	String SERVICE_FORM = "jeecg-desform";

	/**
	 * gateway Truyền đường dẫn gốc qua header basePath
	 */
	String X_GATEWAY_BASE_PATH = "X_GATEWAY_BASE_PATH";

}
